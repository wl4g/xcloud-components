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
package com.wl4g.components.rpc.springcloud.dubbo;

import com.alibaba.dubbo.config.AbstractConfig;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.DubboFeignBuilder;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.FeignClientToDubboProviderConfigurer;
import feign.Feign;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.util.Set;

import static com.alibaba.boot.dubbo.util.DubboUtils.BASE_PACKAGES_PROPERTY_NAME;
import static com.alibaba.boot.dubbo.util.DubboUtils.DUBBO_PREFIX;
import static java.util.Collections.emptySet;

@SuppressWarnings("unchecked")
@Configuration
@ConditionalOnProperty(prefix = DUBBO_PREFIX, name = "enabled", matchIfMissing = true, havingValue = "true")
@ConditionalOnClass(AbstractConfig.class)
public class SpringCloudDubboAutoConfiguration {

	@ConditionalOnProperty(name = BASE_PACKAGES_PROPERTY_NAME)
	@ConditionalOnClass(ConfigurationPropertySources.class)
	@Bean
	public FeignClientToDubboProviderConfigurer feignClientToDubboProviderConfigurer(Environment environment) {
		Set<String> packagesToScan = environment.getProperty(BASE_PACKAGES_PROPERTY_NAME, Set.class, emptySet());
		return new FeignClientToDubboProviderConfigurer(packagesToScan);
	}

	@Bean
	public Feign.Builder dubboFeignBuilder() {
		return new DubboFeignBuilder();
	}

}