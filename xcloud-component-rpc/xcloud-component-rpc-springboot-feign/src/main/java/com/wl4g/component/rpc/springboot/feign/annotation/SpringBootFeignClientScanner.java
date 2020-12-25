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

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wl4g.component.common.log.SmartLogger;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.endsWithAny;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * {@link SpringBootFeignClientScanner}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
class SpringBootFeignClientScanner extends ClassPathBeanDefinitionScanner {
	protected final SmartLogger log = getLogger(getClass());

	@Nullable
	private final Class<?>[] defaultConfiguration;

	public SpringBootFeignClientScanner(BeanDefinitionRegistry registry, Class<?>[] defaultConfiguration) {
		super(registry, true);
		this.defaultConfiguration = defaultConfiguration;
		registerFilters();
	}

	void registerFilters() {
		// include service interfaces
		addIncludeFilter(new AnnotationTypeFilter(SpringBootFeignClient.class, true, true)); // [MARK1]

		// exclude package-info.java
		addExcludeFilter(new TypeFilter() {
			@Override
			public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
				String className = metadataReader.getClassMetadata().getClassName();
				return endsWithAny(className, "package-info", "module-info.java");
			}
		});
	}

	@Override
	public Set<BeanDefinitionHolder> doScan(String... basePackages) {
		Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);
		if (beanDefinitions.isEmpty()) {
			log.warn("No spring boot feign client is found in package '" + Arrays.toString(basePackages) + "'.");
		}

		for (BeanDefinitionHolder holder : beanDefinitions) {
			GenericBeanDefinition definition = (GenericBeanDefinition) holder.getBeanDefinition();
			MergedAnnotation<SpringBootFeignClient> feignClient = ((ScannedGenericBeanDefinition) definition).getMetadata()
					.getAnnotations().get(SpringBootFeignClient.class);
			// Must existing. see:#MARK1
			// if (!feignClient.isPresent())
			// continue;

			MergedAnnotation<RequestMapping> requestMapping = ((ScannedGenericBeanDefinition) definition).getMetadata()
					.getAnnotations().get(RequestMapping.class);

			String beanClassName = definition.getBeanClassName();
			definition.setBeanClass(SpringBootFeignFactoryBean.class);

			definition.setPrimary(feignClient.getBoolean("primary"));
			definition.getPropertyValues().add("proxyInterface", beanClassName);
			definition.getPropertyValues().add("baseUrl", getRequestBaseUrl(feignClient));
			definition.getPropertyValues().add("path", getRequestMappingPath(requestMapping));
			definition.getPropertyValues().add("decode404", feignClient.getBoolean("decode404"));
			definition.getPropertyValues().add("logLevel", feignClient.getValue("logLevel").orElse(null));
			definition.getPropertyValues().add("configuration", feignClient.getClassArray("configuration"));
			definition.getPropertyValues().add("connectTimeout", feignClient.getLong("connectTimeout"));
			definition.getPropertyValues().add("readTimeout", feignClient.getLong("readTimeout"));
			definition.getPropertyValues().add("writeTimeout", feignClient.getLong("writeTimeout"));
			definition.getPropertyValues().add("followRedirects", feignClient.getBoolean("followRedirects"));
			// Fallback default configuration.
			definition.getPropertyValues().add("defaultConfiguration", defaultConfiguration);
		}

		return beanDefinitions;
	}

	private String getRequestBaseUrl(MergedAnnotation<SpringBootFeignClient> feignClient) {
		if (feignClient.isPresent()) {
			return feignClient.getString("url"); // base URL
		}
		return "";
	}

	private String getRequestMappingPath(MergedAnnotation<RequestMapping> requestMapping) {
		if (requestMapping.isPresent()) {
			String[] paths = (String[]) requestMapping.getValue("value").get();
			if (nonNull(paths) && paths.length > 0) {
				return paths[0]; // append to url suffix
			}
		}
		return "";
	}

	@Override
	protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
		return beanDefinition.getMetadata().isInterface();
	}

}
