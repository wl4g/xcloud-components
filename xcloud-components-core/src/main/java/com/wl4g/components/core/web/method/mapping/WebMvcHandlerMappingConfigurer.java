/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.components.core.web.method.mapping;

import static com.wl4g.components.common.collection.Collections2.safeList;
import static java.lang.String.format;
import static java.util.Collections.synchronizedMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.handler.AbstractHandlerMethodMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import static org.springframework.core.annotation.AnnotationAwareOrderComparator.INSTANCE;

import com.wl4g.components.common.collection.CollectionUtils2;

/**
 * Global web MVC {@link RequestMapping} unique handler mapping. (supports multi
 * customization {@link RequestMappingHandlerMapping}) instances. </br>
 * </br>
 * Notes: The usage scenarios and instructions of this global delegation request
 * mapping registration program are as follows: </br>
 * The purpose {@link ServletHandlerMappingSupport} is to use custom global
 * delegation to uniformly register the mapping. Because when an interface
 * annotated with {@link RequestMapping} has multiple subclass instances, spring
 * will automatically register the mapping of two subclass instances by default,
 * which will lead to the exception of registration conflict. For related source
 * code analysis, please refer to:
 * {@link AbstractHandlerMethodMapping#isHandler()}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see {@link org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.EnableWebMvcConfiguration#createRequestMappingHandlerMapping()}
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class WebMvcHandlerMappingConfigurer implements WebMvcRegistrations {

	/**
	 * Directly new create instance, spring is automatically injected into the
	 * container later. refer:
	 * {@link org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration.EnableWebMvcConfiguration#createRequestMappingHandlerMapping()}
	 * {@link org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport#requestMappingHandlerMapping()}
	 * </br>
	 * </br>
	 * Notes: if using @ bean here will result in two instances in the ioc
	 * container.
	 */
	@Override
	public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
		return new SmartServletHandlerMapping();
	}

	/**
	 * Smart delegate MVC/servlet request handler mapping.
	 */
	public static class SmartServletHandlerMapping extends RequestMappingHandlerMapping {

		/**
		 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.MappingRegistry#mappingLookup}
		 */
		private final Map<RequestMappingInfo, HandlerMethod> registeredMappings = synchronizedMap(new LinkedHashMap<>(16));

		@Nullable
		@Autowired(required = false)
		private List<ServletHandlerMappingSupport> handlerMappings;

		private boolean print = false;

		/**
		 * Notes: Must take precedence, otherwise invalid. refer:
		 * {@link org.springframework.web.servlet.DispatcherServlet#initHandlerMappings()}
		 */
		public SmartServletHandlerMapping() {
			setOrder(HIGHEST_PRECEDENCE); // By default order
		}

		@Override
		protected final void processCandidateBean(String beanName) {
			if (CollectionUtils2.isEmpty(handlerMappings)) {
				if (!print) {
					print = true;
					logger.warn(
							"Unable to execution customization request handler mappings, fallback using spring default handler mapping.");
				}
				// Use default handler mapping.
				super.processCandidateBean(beanName);
				return;
			}

			Class<?> beanType = null;
			try {
				beanType = obtainApplicationContext().getType(beanName);
			} catch (Throwable ex) {
				// An unresolvable bean type, probably from a lazy bean - let's
				// ignore it.
				logger.trace("Could not resolve type for bean '" + beanName + "'", ex);
			}

			if (nonNull(beanType) && isHandler(beanType)) {
				// a. Ensure the external handler mapping is performed first.
				boolean supported = false;
				for (ServletHandlerMappingSupport mapping : safeList(handlerMappings)) {
					// Invoke best matchs handler.
					if (mapping.supports(beanName, beanType)) {
						supported = true;
						logger.info(
								format("The bean: '%s' is being delegated to the best request mapping handler registration: '%s'",
										beanName, mapping));
						mapping.processCandidateBean(beanName);
					}
				}

				// b. Fallback, using default handler mapping.
				if (!supported) {
					if (!print) {
						print = true;
						logger.info(format("No suitable request mapping processor was found. all handler mappings: %s",
								handlerMappings));
					}
					super.processCandidateBean(beanName);
				}
			}

		}

		/**
		 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.MappingRegistry#register()}
		 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.MappingRegistry#validateMethodMapping()}
		 * {@link org.springframework.web.method.HandlerMethod#equals()}
		 */
		@Override
		public void registerMapping(RequestMappingInfo mapping, Object handler, Method method) {
			doSmartOverrideRegisterMapping(mapping, handler, method);
		}

		@Override
		public void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
			registerMapping(mapping, handler, method);
		}

		/**
		 * Smart overridable registration request handler mapping
		 * 
		 * @param mapping
		 *            Request mapping information wrapper.
		 * @param handler
		 *            Actual request mapping handler (beanName).
		 * @param method
		 *            Actual request mapping handler method.
		 */
		private final void doSmartOverrideRegisterMapping(RequestMappingInfo mapping, Object handler, Method method) {
			HandlerMethod newHandlerMethod = createHandlerMethod(handler, method);
			HandlerMethod oldHandlerMethod = registeredMappings.get(mapping);

			// Compare the order of the old and new mapping objects to
			// determine whether to perform the logic to override or preserve
			// the old mapping.
			Object newObj = getApplicationContext().getBean((String) newHandlerMethod.getBean());
			Object oldObj = !isNull(oldHandlerMethod) ? getApplicationContext().getBean((String) oldHandlerMethod.getBean())
					: new Object();
			final boolean isOverridable = INSTANCE.compare(newObj, oldObj) < 0;

			if (isOverridable) {
				if (!isNull(oldHandlerMethod) && !oldHandlerMethod.equals(newHandlerMethod)) {
					// re-register
					super.unregisterMapping(mapping);
					logger.warn(format(
							"Override register mapping. Newer bean '%s' method '%s' to '%s': There is already '%s' older bean method '%s' mapped.",
							newHandlerMethod.getBean(), newHandlerMethod, mapping, oldHandlerMethod.getBean(), oldHandlerMethod));
				}
				super.registerMapping(mapping, handler, method);
				registeredMappings.put(mapping, newHandlerMethod);
			} else {
				if ((isNull(oldHandlerMethod) || oldHandlerMethod.equals(newHandlerMethod))) {
					super.registerMapping(mapping, handler, method);
					registeredMappings.put(mapping, newHandlerMethod);
				} else {
					logger.warn(format(
							"Skipped ambiguous mapping. Cannot bean '%s' method '%s' to '%s': There is already '%s' bean method '%s' mapped.",
							newHandlerMethod.getBean(), newHandlerMethod, mapping, oldHandlerMethod.getBean(), oldHandlerMethod));
				}
			}
		}

	}

	/**
	 * In order to enable global delegate of {@link RequestMapping} registration
	 * program supporteds.
	 */
	public static abstract class ServletHandlerMappingSupport extends RequestMappingHandlerMapping {

		private volatile SmartServletHandlerMapping delegate;

		public ServletHandlerMappingSupport() {
			setOrder(Ordered.HIGHEST_PRECEDENCE + 10); // By default order
		}

		@Override
		public final void afterPropertiesSet() {
			// Must ignore, To prevent spring from automatically calling when
			// initializing the container, resulting in duplicate registration.
		}

		/**
		 * Check if the current the supports registration bean handler mapping.
		 * 
		 * @param beanName
		 * @param beanType
		 * @return
		 */
		protected abstract boolean supports(String beanName, Class<?> beanType);

		// [final] override not allowed.
		@Override
		public final void processCandidateBean(String beanName) {
			super.processCandidateBean(beanName);
		}

		// [final] override not allowed.
		@Override
		public final void registerMapping(RequestMappingInfo mapping, Object handler, Method method) {
			getDelegate().registerMapping(mapping, handler, method);
		}

		// [final] override not allowed.
		@Override
		protected final void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
			getDelegate().registerHandlerMethod(handler, method, mapping);
		}

		/**
		 * The method of lazy loading must be used to obtain the delegate object
		 * here, because the subclass of this class may be created externally
		 * by @ bean earlier than the delegate instance created by
		 * {@link WebMvcConfigurationSupport#requestMappingHandlerMapping()}
		 * 
		 * @return
		 */
		private final SmartServletHandlerMapping getDelegate() {
			if (isNull(delegate)) {
				synchronized (this) {
					if (isNull(delegate)) {
						this.delegate = getApplicationContext().getBean(SmartServletHandlerMapping.class);
						// Must init.
						if (isNull(this.delegate.getApplicationContext())) {
							this.delegate.setApplicationContext(getApplicationContext());
						}
					}
				}
			}
			return this.delegate;
		}

	}

}
