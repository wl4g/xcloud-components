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
package com.wl4g.components.core.web.versions;

import static java.util.Objects.isNull;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.wl4g.components.core.web.method.mapping.WebMvcHandlerMappingConfigurer.ServletHandlerMappingSupport;
import com.wl4g.components.core.web.versions.annotation.ApiVersion;
import com.wl4g.components.core.web.versions.annotation.MultiApiVersion;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * Multi versions {@link RequestMapping} handler mapping auto configuration.
 * </br>
 * </br>
 * Notes: Only valid for all {@link RequestMapping} annotated beans (it will
 * override the spring default {@link RequestMappingHandlerMapping}(servlet)
 * instance).
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-26
 * @sine v1.0
 * @see {@link org.springframework.web.servlet.DispatcherServlet#getHandler()}
 * @see <a href=
 *      "https://blog.csdn.net/sinat_29508581/article/details/89392831">Case2</a>
 * @see <a href=
 *      "http://www.kailaisii.com/archives/%E8%87%AA%E5%AE%9A%E4%B9%89RequestMappingHandlerMapping,%E5%86%8D%E4%B9%9F%E4%B8%8D%E7%94%A8%E5%86%99url%E4%BA%86~">Case1</a>
 * @see <a href=
 *      "https://blog.csdn.net/chuantian3080/article/details/100873706">Case3</a>
 */
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ServletVersionRequestMappingConfigurer {

	@Bean
	public ServletVersionRequestHandlerMapping servletVersionRequestHandlerMapping() {
		return new ServletVersionRequestHandlerMapping();
	}

	/**
	 * {@link ServletVersionRequestHandlerMapping}
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @version v1.0 2020-11-26
	 * @sine v1.0
	 * @see {@link org.springframework.web.servlet.DispatcherServlet#getHandler()}
	 */
	static class ServletVersionRequestHandlerMapping extends ServletHandlerMappingSupport {

		public ServletVersionRequestHandlerMapping() {
			setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
		}

		@Override
		protected boolean supports(String beanName, Class<?> beanType) {
			return hasAnnotation(beanType, ApiVersion.class) || hasAnnotation(beanType, MultiApiVersion.class);
		}

		@Override
		protected RequestCondition<ServletVersionCondition> getCustomTypeCondition(Class<?> handlerType) {
			return createCondition(handlerType);
		}

		@Override
		protected RequestCondition<ServletVersionCondition> getCustomMethodCondition(Method method) {
			return createCondition(method);
		}

		private RequestCondition<ServletVersionCondition> createCondition(AnnotatedElement annotatedElement) {
			MultiApiVersion multiApiVersion = findAnnotation(annotatedElement, MultiApiVersion.class);
			ApiVersion apiVersion = findAnnotation(annotatedElement, ApiVersion.class);
			return (isNull(apiVersion) && isNull(multiApiVersion)) ? null
					: new ServletVersionCondition(multiApiVersion, apiVersion);
		}

	}

}
