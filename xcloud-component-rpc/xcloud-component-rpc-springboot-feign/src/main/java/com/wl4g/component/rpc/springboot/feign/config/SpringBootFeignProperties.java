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
package com.wl4g.component.rpc.springboot.feign.config;

import com.wl4g.component.rpc.springboot.feign.annotation.SpringBootFeignClient;

import feign.Logger;

/**
 * {@link SpringBootFeignProperties}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
public class SpringBootFeignProperties {

	/**
	 * The default absolute base URL or resolvable hostname (the protocol is
	 * optional). Will be used when not set in
	 * {@link SpringBootFeignClient#url()}
	 */
	private String defaultUrl;

	/**
	 * The default request base URL, Will be used when not set in
	 * {@link SpringBootFeignClient#logLevel()}
	 * 
	 * @return
	 */
	private Logger.Level defaultLogLevel = Logger.Level.NONE;

	private int maxIdleConnections = 200;

	/** The keep alive default is 5 minutes. */
	private long keepAliveDuration = 5;

	/** The connect timeout default is 10 seconds. */
	private long connectTimeout = 10 * 1000;

	/** The read timeout default is 10 seconds. */
	private long readTimeout = 10 * 1000;

	/** The write timeout default is 10 seconds. */
	private long writeTimeout = 10 * 1000;

	public String getDefaultUrl() {
		return defaultUrl;
	}

	public void setDefaultUrl(String defaultUrl) {
		this.defaultUrl = defaultUrl;
	}

	public Logger.Level getDefaultLogLevel() {
		return defaultLogLevel;
	}

	public void setDefaultLogLevel(Logger.Level defaultLogLevel) {
		this.defaultLogLevel = defaultLogLevel;
	}

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

	public long getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(long connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public long getReadTimeout() {
		return readTimeout;
	}

	public void setReadTimeout(long readTimeout) {
		this.readTimeout = readTimeout;
	}

	public long getWriteTimeout() {
		return writeTimeout;
	}

	public void setWriteTimeout(long writeTimeout) {
		this.writeTimeout = writeTimeout;
	}

}
