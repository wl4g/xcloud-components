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
package com.wl4g.components.rpc.springcloud.feign;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.annotation.RequestHeaderMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.mvc.method.annotation.PathVariableMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.RequestPartMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestResponseBodyMethodProcessor;
import org.springframework.web.servlet.mvc.method.annotation.ServletCookieValueMethodArgumentResolver;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * {@link FeignProxyMvcConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2019-11-20
 * @sine v1.0
 * @see Thannks refer: https://gitee.com/leecho/spring-cloud-feign-proxy
 */
public class FeignProxyMvcConfiguration implements InitializingBean {

	@Autowired
	private RequestMappingHandlerAdapter adapter;

	@Autowired
	private ConfigurableBeanFactory beanFactory;

	@Override
	public void afterPropertiesSet() throws Exception {
		configureMethodArgumentResolvers();
	}

	/**
	 * Configuring handler method argument resolvers configuration.
	 */
	public void configureMethodArgumentResolvers() {
		List<HandlerMethodArgumentResolver> resolvers = new ArrayList<>(adapter.getArgumentResolvers());

		// Supported for @PathVariable
		resolvers.add(0, new PathVariableMethodArgumentResolver() {
			@Override
			public boolean supportsParameter(MethodParameter parameter) {
				return super.supportsParameter(initInterfaceMethodParameter(parameter, PathVariable.class));
			}

			@Override
			protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
				return super.createNamedValueInfo(initInterfaceMethodParameter(parameter, PathVariable.class));
			}

			@Override
			public void contributeMethodArgument(MethodParameter parameter, Object value, UriComponentsBuilder builder,
					Map<String, Object> uriVariables, ConversionService conversionService) {
				super.contributeMethodArgument(initInterfaceMethodParameter(parameter, PathVariable.class), value, builder,
						uriVariables, conversionService);
			}
		});

		// Supported for @RequestHeader
		resolvers.add(0, new RequestHeaderMethodArgumentResolver(beanFactory) {
			@Override
			public boolean supportsParameter(MethodParameter parameter) {
				return super.supportsParameter(initInterfaceMethodParameter(parameter, RequestHeader.class));
			}

			@Override
			protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
				return super.createNamedValueInfo(initInterfaceMethodParameter(parameter, RequestHeader.class));
			}
		});

		// Supported for @CookieValue
		resolvers.add(0, new ServletCookieValueMethodArgumentResolver(beanFactory) {
			@Override
			public boolean supportsParameter(MethodParameter parameter) {
				return super.supportsParameter(initInterfaceMethodParameter(parameter, CookieValue.class));
			}

			@Override
			protected NamedValueInfo createNamedValueInfo(MethodParameter parameter) {
				return super.createNamedValueInfo(initInterfaceMethodParameter(parameter, CookieValue.class));
			}
		});

		// Supported for @RequestBody and @Valid
		resolvers.add(0, new RequestResponseBodyMethodProcessor(adapter.getMessageConverters()) {
			@Override
			public boolean supportsParameter(MethodParameter parameter) {
				return super.supportsParameter(initInterfaceMethodParameter(parameter, RequestBody.class));
			}

			@Override
			protected void validateIfApplicable(WebDataBinder binder, MethodParameter methodParam) {
				super.validateIfApplicable(binder, initInterfaceMethodParameter(methodParam, Valid.class));
			}
		});

		// Supported for @RequestPart
		resolvers.add(0, new RequestPartMethodArgumentResolver(adapter.getMessageConverters()) {
			@Override
			public boolean supportsParameter(MethodParameter parameter) {
				return super.supportsParameter(initInterfaceMethodParameter(parameter, RequestPart.class));
			}

			@Override
			public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest request,
					WebDataBinderFactory binderFactory) throws Exception {
				return super.resolveArgument(initInterfaceMethodParameter(parameter, RequestPart.class), mavContainer, request,
						binderFactory);
			}
		});

		adapter.setArgumentResolvers(resolvers);
	}

	/**
	 * Initializing handler method parameter with interface class.
	 * 
	 * @param parameter
	 * @param annotationType
	 * @return
	 */
	private MethodParameter initInterfaceMethodParameter(MethodParameter parameter, Class<? extends Annotation> annotationType) {
		if (!parameter.hasParameterAnnotation(annotationType)) {
			for (Class<?> interfaceCls : parameter.getDeclaringClass().getInterfaces()) {
				try {
					Method method = interfaceCls.getMethod(parameter.getMethod().getName(),
							parameter.getMethod().getParameterTypes());
					MethodParameter itfParameter = new MethodParameter(method, parameter.getParameterIndex());
					if (itfParameter.hasParameterAnnotation(annotationType)) {
						parameter.initParameterNameDiscovery(new DefaultParameterNameDiscoverer());
						// GenericTypeResolver.resolveParameterType(itfParameter,
						// interfaceCls);
						itfParameter.withContainingClass(interfaceCls);
						return itfParameter;
					}
				} catch (NoSuchMethodException e) {
					continue;
				}
			}
		}
		return parameter;
	}

}