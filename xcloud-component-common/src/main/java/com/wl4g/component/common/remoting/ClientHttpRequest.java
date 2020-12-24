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
package com.wl4g.component.common.remoting;

import java.io.IOException;

import com.wl4g.component.common.remoting.parse.HttpOutputMessage;

/**
 * Represents a client-side HTTP request. Created via an implementation of the
 * {@link ClientHttpRequestFactory}.
 *
 * <p>
 * A {@code ClientHttpRequest} can be {@linkplain #execute() executed},
 * receiving a {@link ClientHttpResponse} which can be read from.
 * 
 * @see ClientHttpRequestFactory#createRequest(java.net.URI, HttpMethod)
 */
public interface ClientHttpRequest extends HttpRequest, HttpOutputMessage {

	/**
	 * Execute this request, resulting in a {@link ClientHttpResponse} that can
	 * be read.
	 * 
	 * @return the response result of the execution
	 * @throws IOException
	 *             in case of I/O errors
	 */
	ClientHttpResponse execute() throws IOException;

}