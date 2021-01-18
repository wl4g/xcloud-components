package com.wl4g.component.rpc.springboot.feign.config;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;

import org.springframework.context.annotation.Bean;

import static com.wl4g.component.rpc.springboot.feign.config.SpringBootFeignConfigurer.*;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;

import feign.Client;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;

@ConditionalOnExpression(KEY_OKHTTP_EXPRESSION)
@ConditionalOnClass(OkHttpClient.class)
public class OkhttpFeignClientConfiguration {
	@Bean
	public ConnectionPool okHttp3ConnectionPool(SpringBootFeignProperties config) {
		return new ConnectionPool(config.getMaxIdleConnections(), config.getKeepAliveDuration(), MINUTES);
	}

	@Bean(BEAN_FEIGN_CLIENT)
	public Client okHttpFeignClient(SpringBootFeignProperties config, ConnectionPool pool) {
		OkHttpClient delegate = new OkHttpClient().newBuilder().connectionPool(pool)
				.connectTimeout(config.getConnectTimeout(), MILLISECONDS).readTimeout(config.getReadTimeout(), MILLISECONDS)
				.writeTimeout(config.getWriteTimeout(), MILLISECONDS).build();
		return new feign.okhttp.OkHttpClient(delegate);
	}
}