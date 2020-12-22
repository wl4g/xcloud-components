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
package com.wl4g.component.rpc.feign.context.interceptor;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.alibaba.cloud.sentinel.custom.SentinelAutoConfiguration;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.common.web.WebUtils2;
import com.wl4g.component.rpc.feign.context.RpcContextHolder;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/***
 * {@link SentinelFeignContextConfigurer}
 *
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-07
 * @sine v1.0
 * @see
 */
@Configuration
@ConditionalOnClass(SentinelAutoConfiguration.class)
@ConditionalOnBean(SentinelAutoConfiguration.class)
@ConditionalOnWebApplication(type = Type.SERVLET)
public class SentinelFeignContextConfigurer implements WebMvcConfigurer {
	protected final SmartLogger log = getLogger(getClass());

	@Bean
	public ServletSentinelConsumerContextInterceptor servletSentinelConsumerContextInterceptor() {
		return new ServletSentinelConsumerContextInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(servletSentinelConsumerContextInterceptor()).addPathPatterns("/**");
	}

	// TODO
	
	/**
	 * Servlet sentinel request context handler interceptor.
	 */
	static class ServletSentinelConsumerContextInterceptor implements RequestInterceptor, HandlerInterceptor {

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			// TODO Auto-generated method stub
			return true;
		}

		@Override
		public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
				throws Exception {
			// TODO Auto-generated method stub
		}

		@Override
		public void apply(RequestTemplate template) {
			// TODO Auto-generated method stub

		}

	}

	static class ServletSentinelFeignProviderContextInterceptor implements HandlerInterceptor {

		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			// Obtain current request parameters and save to rpc context
			RpcContextHolder.get().setAttachments(WebUtils2.getFirstParameters(request));
			return true;
		}

	}

}
