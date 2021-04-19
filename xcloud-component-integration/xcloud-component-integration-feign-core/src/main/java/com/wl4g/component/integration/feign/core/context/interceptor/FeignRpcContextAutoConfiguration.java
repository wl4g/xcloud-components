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
package com.wl4g.component.integration.feign.core.context.interceptor;

import org.springframework.context.annotation.Bean;

import com.wl4g.component.integration.feign.core.context.interceptor.WebMvcRequestInterceptorConfigurer.WebMvcRequestHandlerInterceptor;

/***
 * Auto-configuration(client|server)
 *
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-07
 * @sine v1.0
 * @see
 */
public class FeignRpcContextAutoConfiguration {

	@Bean
	public HeadersFeignRequestInterceptor headersFeignRequestInterceptor() {
		return new HeadersFeignRequestInterceptor();
	}

	@Bean
	public RpcContextConsumerRequestInterceptor rpcContextFeignRequestInterceptor() {
		return new RpcContextConsumerRequestInterceptor();
	}

	@Bean
	public RpcContextProviderProxyInterceptor rpcContextProviderProxyInterceptor() {
		return new RpcContextProviderProxyInterceptor();
	}

	@Bean
	public WebMvcRequestHandlerInterceptor webMvcRequestHandlerInterceptor() {
		return new WebMvcRequestHandlerInterceptor();
	}

	@Bean
	public WebMvcRequestInterceptorConfigurer webServletRequestInterceptorConfigurer(
			WebMvcRequestHandlerInterceptor interceptor) {
		return new WebMvcRequestInterceptorConfigurer(interceptor);
	}

}