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
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.wl4g.component.rpc.springboot.feign.context.RpcContextHolder;
import com.wl4g.component.rpc.springcloud.feign.context.HytrixFeignRpcContextHolder;

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
	public HytrixFeignContextServletInterceptor hytrixFeignContextServletInterceptor() {
		return new HytrixFeignContextServletInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(hytrixFeignContextServletInterceptor()).addPathPatterns("/**");
	}

	/**
	 * hytrix Servlet request context parameters interceptor. </br>
	 * </br>
	 * Refer to <a href=
	 * "https://blog.csdn.net/luliuliu1234/article/details/96472893">HystrixInterceptor</a>
	 */
	static class HytrixFeignContextServletInterceptor implements HandlerInterceptor {
		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			// RpcContextHolder holder = RpcContextHolder.get();
			// isInstanceOf(HytrixFeignRpcContextHolder.class, holder);
			return true;
		}

		@Override
		public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
				throws Exception {
			((HytrixFeignRpcContextHolder) RpcContextHolder.get()).close();
		}
	}

}
