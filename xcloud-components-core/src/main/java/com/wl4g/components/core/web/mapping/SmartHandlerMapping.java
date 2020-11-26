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
package com.wl4g.components.core.web.mapping;

import static java.lang.String.format;
import static java.util.Objects.nonNull;

import java.lang.reflect.Method;

import static com.wl4g.components.common.lang.ClassUtils2.resolveClassNameOrNull;
import static com.wl4g.components.common.reflect.ReflectionUtils2.makeAccessible;
import static org.springframework.util.ReflectionUtils.findMethod;
import static org.springframework.util.ReflectionUtils.invokeMethod;

/**
 * {@link SmartHandlerMapping}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-12
 * @sine v1.0.0
 * @see
 */
public interface SmartHandlerMapping {

	boolean isSupport(String beanName, Class<?> beanType);

	/**
	 * Refer: </br>
	 * </br>
	 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#detectHandlerMethods(Object)}
	 * </br>
	 * {@link org.springframework.web.reactive.result.method.AbstractHandlerMethodMapping#detectHandlerMethods(Object)}
	 * 
	 * @param handler
	 */
	default void doDetectHandlerMethods(Object handler) {
		if (servletRequestHandlerMappingClass.isInstance(this)) {
			makeAccessible(servletDetectHandlerMethods);
			invokeMethod(servletDetectHandlerMethods, this, handler);
		} else if (reactiveRequestHandlerMappingClass.isInstance(this)) {
			makeAccessible(servletDetectHandlerMethods);
			invokeMethod(reactiveDetectHandlerMethods, this, handler);
		} else {
			throw new IllegalStateException(format("The request handler mapping  must inherit '%s' or '%s'",
					servletRequestHandlerMappingClass.getName(), reactiveRequestHandlerMappingClass.getName()));
		}
	}

	public static final Class<?> servletRequestHandlerMappingClass = resolveClassNameOrNull(
			"org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping");
	public static final Class<?> reactiveRequestHandlerMappingClass = resolveClassNameOrNull(
			"org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping");

	public static final Method servletDetectHandlerMethods = nonNull(servletRequestHandlerMappingClass)
			? findMethod(servletRequestHandlerMappingClass, "detectHandlerMethods", Object.class)
			: null;
	public static final Method reactiveDetectHandlerMethods = nonNull(reactiveRequestHandlerMappingClass)
			? findMethod(reactiveRequestHandlerMappingClass, "detectHandlerMethods", Object.class)
			: null;

}
