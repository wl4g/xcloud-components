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

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findMethodNullable;
import static com.wl4g.component.common.reflect.ReflectionUtils2.invokeMethod;
import static com.wl4g.component.common.reflect.ReflectionUtils2.makeAccessible;
import static java.util.Objects.nonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Map;

import javax.validation.constraints.NotNull;

/**
 * This tool class is specially used for reflection call
 * {@link #rpcContextHolderClass}, which provides very good stickiness for
 * supporting different framework architecture running environments to switch
 * between each other.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-29
 * @sine v1.0
 * @see
 */
public abstract class RpcContextHolderBridgeUtils {

	public static Object invokeStaticGet() {
		if (nonNull(staticGetMethod)) {
			makeAccessible(staticGetMethod);
			return notNullOf(invokeMethod(staticGetMethod, null), "currentRpcContextHolder");
		}
		return null;
	}

	// get/set

	@SuppressWarnings("unchecked")
	public static <T> T invokeGet(String key, @NotNull Class<T> valueType) {
		if (nonNull(getMethod)) {
			Object currentRpcContextHolder = invokeStaticGet();
			makeAccessible(getMethod);
			return (T) invokeMethod(getMethod, currentRpcContextHolder, new Object[] { key, valueType });
		}
		return null;
	}

	public static void invokeSet(String key, Object value) {
		if (nonNull(setMethod)) {
			Object currentRpcContextHolder = invokeStaticGet();
			makeAccessible(setMethod);
			invokeMethod(setMethod, currentRpcContextHolder, new Object[] { key, value });
		}
	}

	// getAttachment/setAttachment/getAttachments/removeAttachment/clearAttachments

	public static String invokeGetAttachment(String key) {
		if (nonNull(getAttachmentMethod)) {
			Object currentRpcContextHolder = invokeStaticGet();
			makeAccessible(getAttachmentMethod);
			return (String) invokeMethod(getAttachmentMethod, currentRpcContextHolder, new Object[] { key });
		}
		return null;
	}

	public static void invokeSetAttachment(String key, String value) {
		if (nonNull(setAttachmentMethod)) {
			Object currentRpcContextHolder = invokeStaticGet();
			makeAccessible(setAttachmentMethod);
			invokeMethod(setAttachmentMethod, currentRpcContextHolder, new Object[] { key, value });
		}
	}

	@SuppressWarnings("unchecked")
	public static Map<String, String> invokeGetAttachments(String key) {
		if (nonNull(getAttachmentsMethod)) {
			Object currentRpcContextHolder = invokeStaticGet();
			makeAccessible(getAttachmentsMethod);
			return (Map<String, String>) invokeMethod(getAttachmentsMethod, currentRpcContextHolder, new Object[] { key });
		}
		return null;
	}

	public static void invokeRemoveAttachment(String key) {
		if (nonNull(removeAttachmentMethod)) {
			Object currentRpcContextHolder = invokeStaticGet();
			makeAccessible(removeAttachmentMethod);
			invokeMethod(removeAttachmentMethod, currentRpcContextHolder, new Object[] { key });
		}
	}

	public static void invokeClearAttachments() {
		if (nonNull(clearAttachmentsMethod)) {
			Object currentRpcContextHolder = invokeStaticGet();
			makeAccessible(clearAttachmentsMethod);
			invokeMethod(clearAttachmentsMethod, currentRpcContextHolder);
		}
	}

	// (Reference) get/set

	@SuppressWarnings("unchecked")
	public static <T> T invokeGetRef(String key, @NotNull Class<T> valueType) {
		if (nonNull(getReferenceMethod) && nonNull(referenceKeyClass)) {
			try {
				Constructor<?> constructor = referenceKeyClass.getConstructor(String.class);
				makeAccessible(constructor);
				Object referenceKey = constructor.newInstance(key);
				makeAccessible(getReferenceMethod);
				return (T) invokeMethod(getReferenceMethod, invokeStaticGet(), new Object[] { referenceKey, valueType });
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
		return null;
	}

	public static void invokeSetRef(String key, Object value) {
		if (nonNull(setReferenceMethod) && nonNull(referenceKeyClass)) {
			try {
				Constructor<?> constructor = referenceKeyClass.getConstructor(String.class);
				makeAccessible(constructor);
				Object referenceKey = constructor.newInstance(key);
				makeAccessible(setReferenceMethod);
				invokeMethod(setReferenceMethod, invokeStaticGet(), new Object[] { referenceKey, value });
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}

	// Helper methods.

	/**
	 * Check current runtime has {@link RpcContextHolder} class
	 * 
	 * @return
	 */
	public static boolean hasRpcContextHolderClass() {
		return nonNull(rpcContextHolderClass);
	}

	public static final String rpcContextHolderClassName = "com.wl4g.component.rpc.feign.core.context.RpcContextHolder";
	public static final String rpcContextHolderReferenceKeyClassName = "com.wl4g.component.rpc.feign.core.context.RpcContextHolder.ReferenceKey";

	public static final Class<?> rpcContextHolderClass = resolveClassNameNullable(rpcContextHolderClassName);
	public static final Class<?> referenceKeyClass = resolveClassNameNullable(rpcContextHolderReferenceKeyClassName);

	public static final Method staticGetMethod = findMethodNullable(rpcContextHolderClass, "get");

	public static final Method getMethod = findMethodNullable(rpcContextHolderClass, "get", String.class, Class.class);
	public static final Method setMethod = findMethodNullable(rpcContextHolderClass, "set", String.class, Object.class);

	public static final Method getAttachmentMethod = findMethodNullable(rpcContextHolderClass, "getAttachment", String.class);
	public static final Method setAttachmentMethod = findMethodNullable(rpcContextHolderClass, "setAttachment", String.class,
			String.class);
	public static final Method getAttachmentsMethod = findMethodNullable(rpcContextHolderClass, "getAttachments");
	public static final Method removeAttachmentMethod = findMethodNullable(rpcContextHolderClass, "removeAttachment");
	public static final Method clearAttachmentsMethod = findMethodNullable(rpcContextHolderClass, "clearAttachments");

	public static final Method getReferenceMethod = findMethodNullable(rpcContextHolderClass, "get", referenceKeyClass,
			Class.class);
	public static final Method setReferenceMethod = findMethodNullable(rpcContextHolderClass, "set", referenceKeyClass,
			Object.class);

}
