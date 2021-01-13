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

import com.wl4g.component.rpc.springboot.feign.annotation.mvc.SpringMvcContract;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.ConfigurationProperties;

//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
//import org.springframework.boot.system.JavaVersion;
//
//import feign.http2client.Http2Client;
import feign.Client;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

//import java.net.http.HttpClient;
//import java.time.Duration;

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
	@ConfigurationProperties(prefix = KEY_PREFIX)
	public SpringBootFeignProperties springBootFeignProperties() {
		return new SpringBootFeignProperties();
	}

	@Bean(BEAN_SPRINGMVC_CONTRACT)
	public SpringMvcContract springMvcContract() {
		return new SpringMvcContract();
	}

	@Bean
	public ConnectionPool okHttp3ConnectionPool(SpringBootFeignProperties config) {
		return new ConnectionPool(config.getMaxIdleConnections(), config.getKeepAliveDuration(), MINUTES);
	}

	@Bean(BEAN_FEIGN_CLIENT)
	@ConditionalOnExpression(KEY_CLIENT_EXPRESSION)
	public Client okHttpFeignClient(SpringBootFeignProperties config, ConnectionPool pool) {
		OkHttpClient delegate = new OkHttpClient().newBuilder().connectionPool(pool)
				.connectTimeout(config.getConnectTimeout(), MILLISECONDS).readTimeout(config.getReadTimeout(), MILLISECONDS)
				.writeTimeout(config.getWriteTimeout(), MILLISECONDS).build();
		return new feign.okhttp.OkHttpClient(delegate);
	}

	// @Bean(BEAN_FEIGN_CLIENT)
	// @ConditionalOnJava(JavaVersion.ELEVEN)
	// @ConditionalOnExpression(KEY_CLIENT_EXPRESSION)
	// @ConditionalOnClass(HttpClient.class)
	// public Client http2FeignClient() {
	// HttpClient httpClient =
	// HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS)
	// .version(HttpClient.Version.HTTP_2).connectTimeout(Duration.ofMillis(config.getConnectTimeout())).build();
	// return new Http2Client(httpClient);
	// }

	public static final String BEAN_FEIGN_CLIENT = "springBootFeignClient";
	public static final String BEAN_SPRINGMVC_CONTRACT = "springBootFeignMvcContract";
	public static final String KEY_PREFIX = "spring.boot.xcloud.feign";
	public static final String KEY_CLIENT_EXPRESSION = "'okhttp3'.equalsIgnoreCase('${" + KEY_PREFIX
			+ ".client-provider:okhttp3}')";

}
