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
package com.wl4g.components.rpc.core;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.wl4g.components.common.lang.ClassUtils2;
import com.wl4g.components.core.web.method.mapping.WebMvcHandlerMappingConfigurer;
import com.wl4g.components.core.web.method.mapping.WebMvcHandlerMappingConfigurer.CustomRequestMappingHandlerCondition;

/**
 * {@link DistributedAdaptiveAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-16
 * @sine v1.0
 * @see
 */
@Configuration
public class DistributedAdaptiveAutoConfiguration {

	/**
	 * It is used to manage the conditions under which the feign proxy mapping
	 * handler is automatically injected in different distributed run modes.
	 * 
	 * @see {@link WebMvcHandlerMappingConfigurer.SmartServletHandlerMapping#isHandler(Class)}
	 * @see {@link FeignClient}
	 */
	@Bean
	public CustomRequestMappingHandlerCondition distributedRequestMappingHandlerCondition() {
		// TODO
		return beanType -> true/*ClassUtils2.getPackageName(beanType).startsWith("")*/;
	}

}
