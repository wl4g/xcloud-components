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
package com.wl4g.component.rpc.springboot.feign.annotation;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

import feign.Logger.Level;

import static java.util.Objects.nonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link SpringBootFeignClientsRegistrar}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
public class SpringBootFeignClientsRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

	@SuppressWarnings("unused")
	private ResourceLoader resourceLoader;

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		AnnotationAttributes attrs = AnnotationAttributes
				.fromMap(metadata.getAnnotationAttributes(EnableSpringBootFeignClients.class.getName()));
		if (nonNull(attrs)) {
			List<String> basePackages = new ArrayList<>();
			for (String pkg : attrs.getStringArray("basePackages")) {
				if (StringUtils.hasText(pkg)) {
					basePackages.add(pkg);
				}
			}
			SpringBootFeignClientScanner scanner = new SpringBootFeignClientScanner(registry, attrs.getString("defaultUrl"),
					attrs.getClassArray("defaultConfiguration"), (Level) attrs.get("defaultLogLevel"));
			scanner.doScan(StringUtils.toStringArray(basePackages));
		}

	}

}
