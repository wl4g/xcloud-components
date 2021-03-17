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
package com.wl4g.component.core.page;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;

import java.lang.reflect.Method;

import javax.validation.constraints.NotNull;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Service;

import com.wl4g.component.common.bridge.RpcContextHolderBridges;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.common.matching.RegexMatcher;
import com.wl4g.component.core.framework.proxy.SmartProxyInterceptor;

/**
 * {@link ReleasePageProxyInterceptor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-03-17
 * @sine v1.0
 * @see
 */
public class ReleasePageProxyInterceptor implements SmartProxyInterceptor {
	protected final SmartLogger log = getLogger(getClass());

	@Override
	public int getOrder() {
		return ORDER;
	}

	@Override
	public boolean supportTypeProxy(Object target, Class<?> actualOriginalTargetClass) {
		return checkSupportTypeProxy(target, actualOriginalTargetClass);
	}

	@Override
	public boolean supportMethodProxy(Object target, Method method, Class<?> actualOriginalTargetClass, Object... args) {
		return checkSupportMethodProxy(target, method, actualOriginalTargetClass, args);
	}

	@Override
	public Object postHandle(@NotNull Object target, @NotNull Method method, Object[] parameters, Object result,
			@NotNull Throwable ex) {
		PageHolder.release();
		return result;
	}

	public static boolean checkSupportTypeProxy(Object target, Class<?> actualOriginalTargetClass) {
		if (hasAnnotation(actualOriginalTargetClass, Service.class)) {
			return true;
		}
		return defaultSelectTypeMatcher.matches(actualOriginalTargetClass.getSimpleName());
	}

	public static boolean checkSupportMethodProxy(Object target, Method method, Class<?> actualOriginalTargetClass,
			Object... args) {
		return defaultSelectMethodMatcher.matches(method.getName());
	}

	private static final RegexMatcher defaultSelectTypeMatcher = new RegexMatcher(false, "([\\w]+?)Service", "([\\w]+?)Dao",
			"([\\w]+?)Data", "([\\w]+?)Dal", "([\\w]+?)Handler");

	private static final RegexMatcher defaultSelectMethodMatcher = new RegexMatcher(false, "find([\\w]+?)", "get([\\w]+?)",
			"load([\\w]+?)", "query([\\w]+?)", "qry([\\w]+?)", "extract([\\w]+?)", "obtain([\\w]+?)", "select([\\w]+?)");

	public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 50;

	/**
	 * Automatic release interceptors are configured only when running in
	 * standalone mode.
	 */
	@ConditionalOnMissingClass(RpcContextHolderBridges.rpcContextHolderClassName)
	public static class ReleasePageProxyInterceptorAutoConfiguration {
		@Bean
		public ReleasePageProxyInterceptor releasePageProxyInterceptor() {
			return new ReleasePageProxyInterceptor();
		}
	}

}