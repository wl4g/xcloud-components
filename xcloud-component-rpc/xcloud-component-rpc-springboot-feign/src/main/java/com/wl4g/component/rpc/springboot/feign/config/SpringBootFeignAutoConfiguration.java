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

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import org.springframework.context.annotation.Bean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
//import org.springframework.boot.system.JavaVersion;
//
//import feign.http2client.Http2Client;
import feign.Client;

//import java.net.http.HttpClient;
//import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * {@link SpringBootFeignAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
public class SpringBootFeignAutoConfiguration {

	@Bean
	public SpringBootFeignProperties istioFeignProperties() {
		return new SpringBootFeignProperties();
	}

	@Bean
	public ConnectionPool okHttp3ConnectionPool(SpringBootFeignProperties config) {
		return new ConnectionPool(config.getMaxIdleConnections(), config.getKeepAliveDuration(), TimeUnit.MINUTES);
	}

	@Bean(BEAN_FEIGN_CLIENT)
	@ConditionalOnExpression("'okhttp3'.equals('${feign.httpclient:okhttp3}')")
	public Client okHttpFeignClient(SpringBootFeignProperties config, ConnectionPool connectionPool) {
		OkHttpClient delegate = new OkHttpClient().newBuilder().connectionPool(connectionPool)
				.connectTimeout(config.getConnectTimeout(), TimeUnit.MILLISECONDS)
				.readTimeout(config.getReadTimeout(), TimeUnit.MILLISECONDS)
				.writeTimeout(config.getWriteTimeout(), TimeUnit.MILLISECONDS).build();
		return new feign.okhttp.OkHttpClient(delegate);
	}

	// @Bean(BEAN_FEIGN_CLIENT)
	// @ConditionalOnJava(JavaVersion.ELEVEN)
	// @ConditionalOnExpression("'http2Client'.equals('${feign.httpclient:okhttp3}')")
	// @ConditionalOnClass(HttpClient.class)
	// public Client http2FeignClient() {
	// HttpClient httpClient =
	// HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS)
	// .version(HttpClient.Version.HTTP_2).connectTimeout(Duration.ofMillis(config.getConnectTimeout())).build();
	// return new Http2Client(httpClient);
	// }

	public static final String BEAN_FEIGN_CLIENT = "istioFeignClient";

}
