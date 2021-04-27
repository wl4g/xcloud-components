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
package com.wl4g.component.integration.feign.core.context.internal;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;

import java.io.IOException;
import java.lang.reflect.Type;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.integration.feign.core.context.RpcContextHolder;
import com.wl4g.component.integration.feign.core.context.internal.FeignContextCoprocessor.Invokers;

import feign.FeignException;
import feign.Response;
import feign.codec.DecodeException;
import feign.codec.Decoder;

/**
 * {@link FeignContextDecoder}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-28
 * @sine v1.0
 * @see {@link feign.SynchronousMethodHandler#executeAndDecode()}
 */
public class FeignContextDecoder implements Decoder {
	private final SmartLogger log = getLogger(feign.Logger.class);

	private final Decoder decoder;

	public FeignContextDecoder(Decoder decoder) {
		this.decoder = notNullOf(decoder, "decoder");
	}

	@Override
	public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
		try {
			return decoder.decode(response, type);
		} finally {
			// The RPC call has responded and the attachment info should be
			// extracted from it.
			try {
				FeignRpcContextBinders.bindAttachmentsFromFeignResposne(response);

				// After called RPC, first cleanup context, reference:
				// dubbo-2.7.4.1↓:ConsumerContextFilter.java
				RpcContextHolder.removeContext();

				// Call coprocessor.
				Invokers.afterConsumerExecution(response, type);
			} catch (Exception e2) {
				log.warn("Cannot bind feign response attachments to current RpcContext", e2);
			}
		}
	}

}
