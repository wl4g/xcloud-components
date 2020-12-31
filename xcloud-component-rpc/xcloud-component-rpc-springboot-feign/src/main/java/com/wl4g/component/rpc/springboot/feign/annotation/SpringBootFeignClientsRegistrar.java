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
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.StringUtils.hasText;
import static com.wl4g.component.rpc.springboot.feign.annotation.EnableSpringBootFeignClients.*;

import java.util.HashSet;
import java.util.Set;

/**
 * {@link SpringBootFeignClientsRegistrar}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
class SpringBootFeignClientsRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

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
			/**
			 * 注：这里需手动合并，因为当@EnableSpringBootFeignClients引用了@EnableFeignClients时，
			 * 且没有依赖spring-cloud-openfeign包时，就不能合并属性值？？？
			 * 如，配了value就只能获取value的值，而无法获取被@AliasFor的basePackages的值，稳妥起见手动合并。
			 */
			Set<String> scanBasePackages = getScanBasePackages(metadata, attrs).stream().filter(pkg -> !isBlank(pkg))
					.collect(toSet());
			ExcludeSelfFeignClientsFilter.setScanBasePackages(scanBasePackages.toArray(new String[0]));

			SpringBootFeignClientScanner scanner = new SpringBootFeignClientScanner(registry,
					attrs.getClassArray(DEFAULT_CONFIGURATION));
			scanner.doScan(StringUtils.toStringArray(scanBasePackages));
		}
	}

	private Set<String> getScanBasePackages(AnnotationMetadata metadata, AnnotationAttributes attrs) {
		Set<String> scanBasePackages = new HashSet<>();
		for (String pkg : (String[]) attrs.get("value")) {
			if (hasText(pkg)) {
				scanBasePackages.add(pkg);
			}
		}
		for (String pkg : (String[]) attrs.get(SCAN_BASE_PACKAGES)) {
			if (hasText(pkg)) {
				scanBasePackages.add(pkg);
			}
		}
		for (Class<?> clazz : (Class[]) attrs.get(SCAN_BASE_PACKAGE_CLASSES)) {
			scanBasePackages.add(ClassUtils.getPackageName(clazz));
		}
		if (scanBasePackages.isEmpty()) {
			scanBasePackages.add(ClassUtils.getPackageName(metadata.getClassName()));
		}
		return scanBasePackages;
	}

}
