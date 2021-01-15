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
package com.wl4g.component.rpc.springcloud.feign.config;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.reflect.ReflectionUtils2.findField;
import static com.wl4g.component.common.reflect.ReflectionUtils2.setField;
import static java.util.concurrent.ThreadLocalRandom.current;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.reactive.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.reactive.Request;
import org.springframework.cloud.client.loadbalancer.reactive.Response;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientSpecification;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.rpc.springcloud.feign.constant.SpringCloudConfigConstant;

import reactor.core.publisher.Mono;

/**
 * {@link ExtensionLoadbalancerAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-14
 * @sine v1.0
 * @see {@link org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientConfiguration}
 */
@SuppressWarnings("deprecation")
@LoadBalancerClients
@ConditionalOnDiscoveryEnabled
@Configuration(proxyBeanMethods = false)
public class ExtensionLoadbalancerAutoConfiguration {

	// [FIXED] Override default LoadBalancerClientFactory.
	/**
	 * {@link org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration#loadBalancerClientFactory}
	 */
	@Bean
	@Primary
	@ConditionalOnMissingBean
	public LoadBalancerClientFactory extensionLoadBalancerClientFactory(
			ObjectProvider<List<LoadBalancerClientSpecification>> configurations) {
		LoadBalancerClientFactory clientFactory = new ExtensionLoadBalancerClientFactory();
		clientFactory.setConfigurations(configurations.getIfAvailable(Collections::emptyList));
		return clientFactory;
	}

	/**
	 * {@link org.springframework.cloud.loadbalancer.blocking.client.BlockingLoadBalancerClient#choose}
	 */
	@Bean
	@ConditionalOnProperty(name = SpringCloudConfigConstant.KEY_LOADBALANCER_RANDOM + ".enable", matchIfMissing = false)
	public ReactorLoadBalancer<ServiceInstance> randomReactorServiceInstanceLoadBalancer(Environment environment,
			LoadBalancerClientFactory loadBalancerClientFactory) {
		String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
		return new RandomLoadBalancer(loadBalancerClientFactory.getLazyProvider(name, ServiceInstanceListSupplier.class), name);
	}

	/**
	 * Random load balancer.
	 * 
	 * @see {@link org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer.RoundRobinLoadBalancer}
	 */
	public static class RandomLoadBalancer implements ReactorServiceInstanceLoadBalancer {
		protected final SmartLogger log = getLogger(getClass());
		protected final Random random = new Random();

		private final String serviceId;
		private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

		/**
		 * @param serviceInstanceListSupplierProvider
		 *            a provider of {@link ServiceInstanceListSupplier} that
		 *            will be used to get available instances
		 * @param serviceId
		 *            id of the service for which to choose an instance
		 * @param seedPosition
		 *            Round Robin element position marker
		 */
		public RandomLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
				String serviceId) {
			this.serviceId = serviceId;
			this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
		}

		@SuppressWarnings({ "rawtypes" })
		@Override
		public Mono<Response<ServiceInstance>> choose(Request request) {
			if (serviceInstanceListSupplierProvider != null) {
				ServiceInstanceListSupplier supplier = serviceInstanceListSupplierProvider
						.getIfAvailable(NoopServiceInstanceListSupplier::new);
				return supplier.get().next().map(this::getInstanceResponse);
			}
			return Mono.empty();
		}

		private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances) {
			if (instances.isEmpty()) {
				log.warn("No servers available for service: " + this.serviceId);
				return new EmptyResponse();
			}

			ServiceInstance instance = instances.get(current().nextInt(0, instances.size()));
			return new DefaultResponse(instance);
		}
	}

	public static class ExtensionLoadBalancerClientFactory extends LoadBalancerClientFactory {
		public ExtensionLoadBalancerClientFactory() {
			super();
			// [FIXED] Override default config type.
			Field defaultConfigTypeField = findField(getClass(), "defaultConfigType", Class.class);
			setField(defaultConfigTypeField, this, ExtensionLoadbalancerAutoConfiguration.class, true);
		}
	}

}
