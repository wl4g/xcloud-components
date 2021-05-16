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

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.web.WebUtils2.PARAM_STACKTRACE;
import static com.wl4g.component.common.web.WebUtils2.isStacktraceRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.wl4g.component.integration.feign.core.context.RpcContextHolder;
import com.wl4g.component.integration.feign.core.context.internal.FeignContextCoprocessor;

import feign.RequestTemplate;

/**
 * {@link SimpleLogTraceFeignContextCoprocessor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-04-27
 * @sine v1.0
 * @see
 */
public class SimpleLogTraceFeignContextCoprocessor implements FeignContextCoprocessor {

	@Override
	public void beforeConsumerExecution(RequestTemplate template) {
		// Pass 'stacktrace' parameter through to the next service
		template.header(PARAM_STACKTRACE, RpcContextHolder.getContext().getAttachment(PARAM_STACKTRACE));
	}

	static class SimpleLogTraceMvcConfigurer implements WebMvcConfigurer {
		private final SimpleLogTraceHandlerInterceptor interceptor;

		public SimpleLogTraceMvcConfigurer(SimpleLogTraceHandlerInterceptor interceptor) {
			this.interceptor = notNullOf(interceptor, "interceptor");
		}

		@Override
		public void addInterceptors(InterceptorRegistry registry) {
			registry.addInterceptor(interceptor).addPathPatterns("/**");
		}
	}

	static class SimpleLogTraceHandlerInterceptor implements HandlerInterceptor {
		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			// Check stacktrace request.
			if (isStacktraceRequest(request)) {
				RpcContextHolder.getContext().setAttachment(PARAM_STACKTRACE, Boolean.TRUE.toString());
			}
			return true;
		}
	}

}
