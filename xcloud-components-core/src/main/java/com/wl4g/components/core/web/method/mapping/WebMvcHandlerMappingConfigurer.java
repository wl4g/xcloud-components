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
import static java.util.Objects.nonNull;

import java.lang.reflect.Method;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.wl4g.components.common.collection.CollectionUtils2;

/**
 * Global web servlet mvc {@link RequestMapping} unique configuration.
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

	@Override
	public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
		return delegateServletRequestHandlerMapping();
	}

	@Bean
	public DelegateServletHandlerMapping delegateServletRequestHandlerMapping() {
		return new DelegateServletHandlerMapping();
	}

	/**
	 * {@link DelegateServletHandlerMapping}
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @version v1.0 2020-11-26
	 * @sine v1.0
	 * @see
	 */
	public static class DelegateServletHandlerMapping extends RequestMappingHandlerMapping {

		@Autowired(required = false)
		protected List<ServletHandlerMappingSupport> handlerMappings;

		private boolean print = false;

		/**
		 * Notes: Must take precedence, otherwise invalid. refer:
		 * {@link org.springframework.web.servlet.DispatcherServlet#initHandlerMappings()}
		 */
		public DelegateServletHandlerMapping() {
			setOrder(HIGHEST_PRECEDENCE);
		}

		@Override
		protected void processCandidateBean(String beanName) {
			if (CollectionUtils2.isEmpty(handlerMappings)) {
				if (!print) {
					print = true;
					logger.warn(
							"Unable to execution customization request handler mappings, fallback using spring default handler mapping.");
				}
				// Fallback, using default handler mapping.
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
				for (ServletHandlerMappingSupport mapping : safeList(handlerMappings)) {
					// Invoke best matchs handler.
					if (mapping.supports(beanName, beanType)) {
						logger.info(format("Delegating best request handler mapping for: %s", mapping));
						mapping.processCandidateBean(beanName);
					}
				}
			}

		}

		@Override
		public final void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
			super.registerHandlerMethod(handler, method, mapping);
		}

	}

	/**
	 * To support handler mapping registered to delegates, refer:
	 * {@link ServletHandlerMappingRegistry}
	 */
	public static abstract class ServletHandlerMappingSupport extends RequestMappingHandlerMapping {

		@Autowired
		private DelegateServletHandlerMapping delegate;

		public ServletHandlerMappingSupport() {
			setOrder(0); // By default order
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
			delegate.registerMapping(mapping, handler, method);
		}

		// [final] override not allowed.
		@Override
		protected final void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
			delegate.registerHandlerMethod(handler, method, mapping);
		}

	}

}
