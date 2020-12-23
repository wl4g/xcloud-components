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
package com.wl4g.component.rpc.springcloud.feign.context.interceptor;

import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static java.util.Objects.isNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.cloud.netflix.hystrix.HystrixAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;
import com.wl4g.component.common.web.WebUtils2;
import com.wl4g.component.rpc.springcloud.feign.context.RpcContextHolder;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/***
 * {@link HytrixFeignContextConfigurer}
 *
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-07
 * @sine v1.0
 * @see
 */
@Configuration
@ConditionalOnClass(HystrixAutoConfiguration.class)
@ConditionalOnBean(HystrixAutoConfiguration.class)
@ConditionalOnWebApplication(type = Type.SERVLET)
public class HytrixFeignContextConfigurer implements WebMvcConfigurer {

	@Bean
	public ServletHytrixFeignConsumerContextInterceptor servletHytrixConsumerContextInterceptor() {
		return new ServletHytrixFeignConsumerContextInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(servletHytrixConsumerContextInterceptor()).addPathPatterns("/**");
	}

	/**
	 * Servlet hytrix request context parameters interceptor. </br>
	 * </br>
	 * Refer to <a href=
	 * "https://blog.csdn.net/luliuliu1234/article/details/96472893">HystrixInterceptor</a>
	 */
	static class ServletHytrixFeignConsumerContextInterceptor implements RequestInterceptor, HandlerInterceptor {

		public static final HystrixRequestVariableDefault<HttpServletRequest> REQUEST = new HystrixRequestVariableDefault<>();

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			// Initializes hystrixrequestcontext in the current thread.
			if (!HystrixRequestContext.isCurrentThreadInitialized()) {
				HystrixRequestContext.initializeContext();
			}
			REQUEST.set(request);
			return true;
		}

		@Override
		public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
				throws Exception {
			if (HystrixRequestContext.isCurrentThreadInitialized()) {
				// Destroy current thread
				HystrixRequestContext.getContextForCurrentThread().shutdown();
			}
		}

		@Override
		public void apply(RequestTemplate template) {
			if (!HystrixRequestContext.isCurrentThreadInitialized()) {
				HystrixRequestContext.initializeContext();
			}
			// Obtain current request parameters save to feign request template
			HttpServletRequest request = ServletHytrixFeignConsumerContextInterceptor.REQUEST.get();
			if (isNull(request)) {
				return;
			}

			// Sets request attachments.
			FeignUtil.addParamsFromServletRequest(template, request);

			// Obtain current rpc context attachments save to feign request
			// template
			safeMap(RpcContextHolder.get().getAttachments()).forEach((name, value) -> template.header(name, value));
		}
	}

	static class ServletHytrixFeignProviderContextInterceptor implements HandlerInterceptor {

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			// Obtain current request parameters and save to rpc context
			RpcContextHolder.get().setAttachments(WebUtils2.getFirstParameters(request));
			return true;
		}

		@Override
		public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
				ModelAndView modelAndView) throws Exception {
			// Sets response attachments.

		}

	}

}
