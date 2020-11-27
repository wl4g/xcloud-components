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
package com.wl4g.components.core.web.versions;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

/**
 * {@link ApiVersionMappingRegistrar}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see
 */
public class ApiVersionMappingRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {

	private ResourceLoader resourceLoader;
	private Environment environment;

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		AnnotationAttributes feignProviderProxiesAttrs = AnnotationAttributes
				.fromMap(metadata.getAnnotationAttributes(EnableApiVersionMapping.class.getName()));
		if (!isNull(feignProviderProxiesAttrs)) {
			registerBeanDefinitions(metadata, feignProviderProxiesAttrs, registry, generateBaseBeanName(metadata, 0));
		}
	}

	protected void registerBeanDefinitions(AnnotationMetadata metadata, AnnotationAttributes annoAttrs,
			BeanDefinitionRegistry registry, String beanName) {

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(FeignProviderProxiesConfigurer.class);
		builder.addPropertyValue("resourceLoader", resourceLoader);
		builder.addPropertyValue("environment", environment);

		registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
	}

	private static String generateBaseBeanName(AnnotationMetadata metadata, int index) {
		return metadata.getClassName() + "#" + ApiVersionMappingRegistrar.class.getSimpleName() + "#" + index;
	}

}
