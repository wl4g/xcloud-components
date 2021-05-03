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
package com.wl4g.component.data.cache;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.lang.TypeConverts.parseLongOrDefault;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

import com.wl4g.component.core.framework.proxy.InvocationChain;
import com.wl4g.component.core.framework.proxy.SmartProxyFilter;
import com.wl4g.component.data.cache.annotation.Cached;
import com.wl4g.component.data.cache.annotation.Cached.DataCacheWrapper;

/**
 * {@link MethodCachingProxyFilter}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-05-02
 * @sine v1.0
 * @see
 */
public class MethodCachingProxyFilter implements SmartProxyFilter {

	protected @Autowired Environment environment;

	protected final Map<Method, DataCacheWrapper> methodMetaCaching;
	protected final ICacheStorage cache;

	public MethodCachingProxyFilter(ICacheStorage cache) {
		this.cache = notNullOf(cache, "dataCache");
		this.methodMetaCaching = new HashMap<>(64);
	}

	@Override
	public int getOrder() {
		return 0;
	}

	@Override
	public boolean supportTypeProxy(Object target, Class<?> actualOriginalTargetClass) {
		return hasAnnotation(actualOriginalTargetClass, Cached.class);
	}

	@Override
	public boolean supportMethodProxy(Object target, Method method, Class<?> actualOriginalTargetClass, Object... args) {
		return hasAnnotation(method, Cached.class);
	}

	// TODO 1.Dirty reading and dirty writing?
	// TODO 2.Support manual cleaning API?
	@Override
	public Object doInvoke(@NotNull InvocationChain chain, @NotNull Object target, @NotNull Method method, Object[] args)
			throws Exception {
		DataCacheWrapper dcw = findDataCacheMeta(target, method);
		String cacheKey = getMethodCacheKey(target, method, args);

		// First get from caching.
		Object cachedValue = cache.get(cacheKey, method.getReturnType());
		if (nonNull(cachedValue)) {
			return cachedValue;
		}

		// Invoke actual method.
		Object result = chain.doInvoke(target, method, args);

		// To cache.
		cache.put(cacheKey, result, dcw.getExpireMs());

		return result;
	}

	protected String getMethodCacheKey(@NotNull Object target, @NotNull Method method, Object[] args) {
		return null;
	}

	/**
	 * Find method annotaton of {@link Cached.DataCacheWrapper} meta
	 * information.
	 * 
	 * @param target
	 * @param method
	 * @return
	 */
	private final DataCacheWrapper findDataCacheMeta(@NotNull Object target, @NotNull Method method) {
		DataCacheWrapper dcw = methodMetaCaching.get(method);
		if (isNull(dcw)) {
			synchronized (this) {
				dcw = methodMetaCaching.get(method);
				if (isNull(dcw)) {
					Cached dc = findAnnotation(method, Cached.class);
					if (isNull(dc)) { // Fallback use declaring type
						dc = findAnnotation(method.getDeclaringClass(), Cached.class);
					}
					String prefix = environment.resolvePlaceholders(dc.name());
					long expireMs = parseLongOrDefault(environment.resolvePlaceholders(dc.expireMs()));
					methodMetaCaching.put(method, new DataCacheWrapper(prefix, expireMs));
				}
			}
		}
		return dcw;
	}

	static class MethodCachingAutoConfiguration {
		@Bean
		public MethodCachingProxyFilter methodCachingProxyFilter(ICacheStorage cache) {
			return new MethodCachingProxyFilter(cache);
		}
	}

}
