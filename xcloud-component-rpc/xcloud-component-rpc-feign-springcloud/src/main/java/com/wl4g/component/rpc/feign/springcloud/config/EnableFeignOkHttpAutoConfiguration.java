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
package com.wl4g.component.rpc.feign.springcloud.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.commons.httpclient.OkHttpClientConnectionPoolFactory;
import org.springframework.cloud.commons.httpclient.OkHttpClientFactory;
import org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.loadbalancer.OnRetryNotEnabledCondition;
import org.springframework.cloud.openfeign.loadbalancer.RetryableFeignBlockingLoadBalancerClient;
import org.springframework.cloud.openfeign.support.FeignHttpClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import feign.Client;

/**
 * 注:当使用{@link ConditionalOnProperty}或{@link ConditionalOnBean}时，此类不执行（自动配置不生效），
 * 还是会使用默认配置:{@link org.springframework.cloud.openfeign.loadbalancer.DefaultFeignLoadBalancerConfiguration#feignClient}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-19
 * @sine v1.0
 * @see https://blog.csdn.net/dingmeinai9020/article/details/102069649
 */
@ConditionalOnClass(feign.okhttp.OkHttpClient.class)
// @ConditionalOnProperty(name = "feign.okhttp.enabled", matchIfMissing = false)
// @ConditionalOnBean(BlockingLoadBalancerClient.class)
// @Import(OkHttpFeignConfiguration.class)
// @AutoConfigureBefore({FeignAutoConfiguration.class,FeignLoadBalancerAutoConfiguration.class})
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class EnableFeignOkHttpAutoConfiguration {

	@Bean
	public okhttp3.ConnectionPool okHttpConnectionPool(FeignHttpClientProperties config,
			OkHttpClientConnectionPoolFactory factory) {
		return factory.create(config.getMaxConnections(), config.getTimeToLive(), config.getTimeToLiveUnit());
	}

	@Bean
	public okhttp3.OkHttpClient okhttpClient(OkHttpClientFactory factory, okhttp3.ConnectionPool pool,
			FeignHttpClientProperties config) {
		return factory.createBuilder(config.isDisableSslValidation())
				.connectTimeout((long) config.getConnectionTimeout(), TimeUnit.MILLISECONDS)
				.followRedirects(config.isFollowRedirects()).connectionPool(pool)
				/* .addInterceptor(interceptor) */.build();
	}

	// See:org.springframework.cloud.openfeign.loadbalancer.DefaultFeignLoadBalancerConfiguration
	// See:org.springframework.cloud.openfeign.loadbalancer.OkHttpFeignLoadBalancerConfiguration

	@Bean
	public feign.okhttp.OkHttpClient feignOkHttpClient(okhttp3.OkHttpClient okhttpClient) {
		return new feign.okhttp.OkHttpClient(okhttpClient);
	}

	@Bean
	@Primary
	@Conditional(OnRetryNotEnabledCondition.class)
	public Client feignClient(feign.okhttp.OkHttpClient feignOkHttpClient, BlockingLoadBalancerClient loadBalancerClient) {
		return new FeignBlockingLoadBalancerClient(feignOkHttpClient, loadBalancerClient);
	}

	// Notes: spring-cloud-loadbalancer-2.2.6.RELEASE.jar Retrying is not
	// supported at this time (please use deprecated
	// spring-cloud-starter-netflix-ribbon)
	@Bean
	@Primary
	@ConditionalOnMissingBean
	@ConditionalOnClass(name = "org.springframework.retry.support.RetryTemplate")
	@ConditionalOnBean(LoadBalancedRetryFactory.class)
	@ConditionalOnProperty(value = "spring.cloud.loadbalancer.retry.enabled", havingValue = "true", matchIfMissing = true)
	public Client feignRetryClient(BlockingLoadBalancerClient loadBalancerClient, feign.okhttp.OkHttpClient feignOkHttpClient,
			List<LoadBalancedRetryFactory> lbRetryFactories) {
		AnnotationAwareOrderComparator.sort(lbRetryFactories);
		return new RetryableFeignBlockingLoadBalancerClient(feignOkHttpClient, loadBalancerClient, lbRetryFactories.get(0));
	}

}