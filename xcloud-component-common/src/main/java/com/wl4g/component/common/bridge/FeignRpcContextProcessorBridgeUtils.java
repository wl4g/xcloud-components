/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.component.common.bridge;

import static com.wl4g.component.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findFieldNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findMethodNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.getField;
import static com.wl4g.component.common.reflect.ReflectionUtils2.invokeMethod;
import static com.wl4g.component.common.reflect.ReflectionUtils2.makeAccessible;
import static java.util.Objects.nonNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * {@link FeignRpcContextProcessorBridgeUtils}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-29
 * @sine v1.0
 * @see
 */
public abstract class FeignRpcContextProcessorBridgeUtils {

	public static int invokeFieldOrder() {
		if (nonNull(ORDER_FIELD)) {
			return (int) getField(ORDER_FIELD, null, true);
		}
		return 0;
	}

	public static boolean invokeCheckSupportTypeProxy(Object target, Class<?> actualOriginalTargetClass) {
		if (nonNull(checkSupportTypeProxyMethod)) {
			makeAccessible(checkSupportTypeProxyMethod);
			return (boolean) invokeMethod(checkSupportTypeProxyMethod, null, new Object[] { target, actualOriginalTargetClass });
		}
		return false;
	}

	public static boolean invokeCheckSupportMethodProxy(Object target, Method method, Class<?> actualOriginalTargetClass,
			Object... args) {
		if (nonNull(checkSupportMethodProxyMethod)) {
			makeAccessible(checkSupportMethodProxyMethod);
			return (boolean) invokeMethod(checkSupportMethodProxyMethod, null,
					new Object[] { target, method, actualOriginalTargetClass, args });
		}
		return false;
	}

	public static boolean hasFeignRpcContextProcessorClass() {
		return nonNull(feignRpcContextProcessorClass);
	}

	public static final String feignRpcContextProcessorClassName = "com.wl4g.component.rpc.feign.core.context.interceptor.FeignRpcContextAutoConfiguration.FeignRpcContextProcessor";
	public static final Class<?> feignRpcContextProcessorClass = resolveClassNameNullable(feignRpcContextProcessorClassName);

	public static final Method checkSupportTypeProxyMethod = findMethodNullable(feignRpcContextProcessorClass,
			"checkSupportTypeProxy", Object.class, Class.class);
	public static final Method checkSupportMethodProxyMethod = findMethodNullable(feignRpcContextProcessorClass,
			"checkSupportMethodProxy", Object.class, Method.class, Class.class, Object[].class);

	public static final Field ORDER_FIELD = findFieldNullable(feignRpcContextProcessorClass, "ORDER", int.class);

}
