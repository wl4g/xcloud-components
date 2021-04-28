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
package com.wl4g.component.integration.feign.core.extension;

import static com.wl4g.component.common.web.WebUtils2.PARAM_STACKTRACE;
import static com.wl4g.component.common.web.WebUtils2.isStacktraceRequest;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

import com.wl4g.component.core.page.PageHolder;
import com.wl4g.component.integration.feign.core.context.RpcContextHolder;
import com.wl4g.component.integration.feign.core.context.internal.FeignContextCoprocessor;

import feign.Response;

/**
 * {@link PageBindingFeignContextCoprocessor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-04-27
 * @sine v1.0
 * @see
 */
public class PageBindingFeignContextCoprocessor implements FeignContextCoprocessor {

	@Override
	public void afterConsumerExecution(@NotNull Response response, Type type) {
		// Load new paging information to the original paging object.(If
		// it is currently the service provider side)
		// @see:com.wl4g.component.data.mybatis.mapper.PreparedBeanMapperInterceptor#postQuery
		PageHolder.current(true);

		// Update executed paging information.
		PageHolder.update();
	}

	@Override
	public void beforeProviderExecution(@Nullable HttpServletRequest request, @NotNull Object target, @NotNull Method method,
			Object[] parameters) {
		// Check stacktrace request.
		if (isStacktraceRequest(request)) {
			RpcContextHolder.getContext().setAttachment(PARAM_STACKTRACE, Boolean.TRUE.toString());
		}
	}

	@Override
	public void afterProviderExecution(@NotNull Object target, @NotNull Method method, Object[] parameters, Object result,
			@NotNull Throwable ex) {
	}

}
