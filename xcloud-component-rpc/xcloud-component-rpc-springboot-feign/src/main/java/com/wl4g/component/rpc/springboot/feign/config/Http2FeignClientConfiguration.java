package com.wl4g.component.rpc.springboot.feign.config;

import static com.wl4g.component.rpc.springboot.feign.config.SpringBootFeignConfigurer.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnJava;
//import org.springframework.boot.system.JavaVersion;
//
//import feign.http2client.Http2Client;

@ConditionalOnExpression(KEY_HTTP2_EXPRESSION)
// @ConditionalOnClass(HttpClient.class)
public class Http2FeignClientConfiguration {

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

}