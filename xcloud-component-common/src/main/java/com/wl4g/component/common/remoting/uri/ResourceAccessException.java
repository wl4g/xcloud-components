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
package com.wl4g.component.common.remoting.uri;

import java.io.IOException;

import com.wl4g.component.common.remoting.exception.RestClientException;

/**
 * Exception thrown when an I/O error occurs.
 */
public class ResourceAccessException extends RestClientException {

	private static final long serialVersionUID = -8513182514355844870L;

	/**
	 * Construct a new {@code ResourceAccessException} with the given message.
	 * 
	 * @param msg
	 *            the message
	 */
	public ResourceAccessException(String msg) {
		super(msg);
	}

	/**
	 * Construct a new {@code ResourceAccessException} with the given message
	 * and {@link IOException}.
	 * 
	 * @param msg
	 *            the message
	 * @param ex
	 *            the {@code IOException}
	 */
	public ResourceAccessException(String msg, IOException ex) {
		super(msg, ex);
	}

}