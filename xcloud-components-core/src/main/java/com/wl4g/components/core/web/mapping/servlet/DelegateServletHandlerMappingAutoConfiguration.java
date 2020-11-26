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
package com.wl4g.components.core.web.mapping.servlet;

import static com.wl4g.components.common.collection.Collections2.safeList;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.wl4g.components.common.collection.CollectionUtils2;
import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.core.web.mapping.SmartHandlerMapping;

/**
 * {@link DelegateServletHandlerMappingAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-26
 * @sine v1.0
 * @see
 */
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DelegateServletHandlerMappingAutoConfiguration implements WebMvcRegistrations {

	@Override
	public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
		return delegateRequestMappingHandlerMapping();
	}

	@Order(Ordered.HIGHEST_PRECEDENCE)
	@Bean
	public DelegateRequestMappingHandlerMapping delegateRequestMappingHandlerMapping() {
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
	public static class DelegateRequestMappingHandlerMapping extends RequestMappingHandlerMapping {

		protected final SmartLogger log = getLogger(getClass());

		@Autowired(required = false)
		protected List<SmartHandlerMapping> handlerMapping;

		@Override
		protected void processCandidateBean(String beanName) {
			if (CollectionUtils2.isEmpty(handlerMapping)) {
				// Skip customize request handler mapping.
				super.processCandidateBean(beanName);
				log.warn(
						"Unable to execution customization request handler mappings, fallback using spring default handler mapping.");
				return;
			}

			Class<?> beanType = null;
			try {
				beanType = obtainApplicationContext().getType(beanName);
			} catch (Throwable ex) {
				// An unresolvable bean type, probably from a lazy bean - let's
				// ignore it.
				if (logger.isTraceEnabled()) {
					logger.trace("Could not resolve type for bean '" + beanName + "'", ex);
				}
			}

			if (beanType != null && isHandler(beanType)) {
				// Multi handler mappings sorts.
				AnnotationAwareOrderComparator.sort(handlerMapping);

				boolean delegated = false;
				for (SmartHandlerMapping mapping : safeList(handlerMapping)) {
					// Invoke best matchs handler.
					if (mapping.isSupport(beanName, beanType)) {
						delegated = true;
						mapping.doDetectHandlerMethods(beanName);
						log.info("Delegating best request handler mapping for: {}", mapping);
						break;
					}
				}
				if (!delegated) {
					log.warn("No suitable request mapping processor was found. all handler mappings: {}", handlerMapping);
				}
			}

		}

	}

}
