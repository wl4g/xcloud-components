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
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static java.util.Objects.nonNull;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.wl4g.components.common.collection.CollectionUtils2;
import com.wl4g.components.common.log.SmartLogger;

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
public class GlobalServletHandlerMappingAutoConfiguration implements WebMvcRegistrations {

	@Override
	public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
		return globalDelegateRequestMappingHandlerMapping();
	}

	@Bean
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
	public DelegateRequestMappingHandlerMapping globalDelegateRequestMappingHandlerMapping() {
		return new DelegateRequestMappingHandlerMapping();
	}

	/**
	 * {@link DelegateRequestMappingHandlerMapping}
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @version v1.0 2020-11-26
	 * @sine v1.0
	 * @see
	 */
	static class DelegateRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

		protected final SmartLogger log = getLogger(getClass());

		@Autowired(required = false)
		protected List<SmartHandlerMapping> handlerMappings;

		private boolean print = false;

		public DelegateRequestMappingHandlerMapping() {
			setOrder(HIGHEST_PRECEDENCE + 10);
		}

		@Override
		protected void processCandidateBean(String beanName) {
			if (CollectionUtils2.isEmpty(handlerMappings)) {
				if (!print) {
					print = true;
					log.warn(
							"Unable to execution customization request handler mappings, fallback using spring default handler mapping.");
				}
				// Fallback request handler mapping.
				super.processCandidateBean(beanName);
				return;
			}

			Class<?> beanType = null;
			try {
				beanType = obtainApplicationContext().getType(beanName);
			} catch (Throwable ex) {
				// An unresolvable bean type, probably from a lazy bean - let's
				// ignore it.
				log.trace("Could not resolve type for bean '" + beanName + "'", ex);
			}

			if (nonNull(beanType) && isHandler(beanType)) {
				boolean matched = false;
				for (SmartHandlerMapping mapping : safeList(handlerMappings)) {
					// Invoke best matchs handler.
					if (mapping.isSupport(beanName, beanType)) {
						matched = true;
						log.info("Delegating best request handler mapping for: {}", mapping);
						mapping.doDetectHandlerMethods(beanName);
						break;
					}
				}
				if (!matched && !print) {
					print = true;
					log.warn("No suitable request mapping processor was found. all handler mappings: {}", handlerMappings);
				}
			}

		}

	}

}
