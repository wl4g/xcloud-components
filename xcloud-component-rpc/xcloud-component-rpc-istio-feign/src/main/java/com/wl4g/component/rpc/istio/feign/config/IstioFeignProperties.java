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
package com.wl4g.component.rpc.istio.feign.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@link IstioFeignProperties}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
@ConfigurationProperties(prefix = "istio.feign", ignoreInvalidFields = true, ignoreUnknownFields = true)
public class IstioFeignProperties {

	private int maxIdleConnections = 200;

	/**
	 * The default is 5 minutes.
	 */
	private long keepAliveDuration = 5;

	/**
	 * Default connection timeout (in milliseconds). The default is 10 seconds.
	 */
	private int connectTimeout = 10 * 1000;
	private int readTimeout = 10 * 1000;
	private int writeTimeout = 10 * 1000;

	public int getMaxIdleConnections() {
		return maxIdleConnections;
	}

	public void setMaxIdleConnections(int maxIdleConnections) {
		this.maxIdleConnections = maxIdleConnections;
	}

	public long getKeepAliveDuration() {
		return keepAliveDuration;
	}

	public void setKeepAliveDuration(long keepAliveDuration) {
		this.keepAliveDuration = keepAliveDuration;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(int readTimeout) {
		this.readTimeout = readTimeout;
	}

	public int getWriteTimeout() {
		return writeTimeout;
	}

	public void setWriteTimeout(int writeTimeout) {
		this.writeTimeout = writeTimeout;
	}
}
