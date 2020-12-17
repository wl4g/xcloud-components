/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
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
 */
package com.wl4g.component.rpc.feign.interceptor;

import static java.util.stream.Collectors.toSet;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;

import feign.RequestInterceptor;

// TODO

/**
 * {@link FeignContextAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-07
 * @sine v1.0
 * @see
 */
public class FeignContextAutoConfiguration {

	// @Bean
	public RequestInterceptor requestInterceptor() {
		return template -> {
			if (!HystrixRequestContext.isCurrentThreadInitialized()) {
				HystrixRequestContext.initializeContext();
			}
			HttpServletRequest request = HystrixRequestContextInterceptor.REQUEST.get();
			if (null == request) {
				return;
			}

			// 获取RequestTemplate中已包含的请求头
			Map<String, Collection<String>> curHeaders = template.headers();
			Set<String> curHeaderNames = null == curHeaders ? new HashSet<>()
					: curHeaders.keySet().stream().map(String::toLowerCase).collect(toSet());

			Enumeration<String> headerNames = request.getHeaderNames();
			if (headerNames != null) {
				while (headerNames.hasMoreElements()) {
					String name = headerNames.nextElement();
					// 如果RequestTemplate头信息中已经包含了该请求头, 跳过
					if (curHeaderNames.contains(name))
						continue;
					String values = request.getHeader(name);
					template.header(name, values);
				}
			}
		};
	}

}
