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
package com.wl4g.component.rpc.feign.core.context.interceptor;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.rpc.feign.core.context.RpcContextHolder;

import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * {@link FeignRpcContextRequestInterceptor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-26
 * @sine v1.0
 * @see
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class FeignRpcContextRequestInterceptor implements RequestInterceptor {
	protected final SmartLogger log = getLogger(getClass());

	/**
	 * {@link feign.SynchronousMethodHandler#executeAndDecode(RequestTemplate, Options)}
	 */
	@Override
	public void apply(RequestTemplate template) {
		try {
			// Before calling RPC, the current context attachment info should be
			// added to the request header.
			FeignRpcContextUtils.writeAttachmentsToFeignRequest(template);
		} finally {
			// The RPC has been called and the local context should be
			// cleaned up immediately.
			RpcContextHolder.get().clearAttachments();
		}
	}

}