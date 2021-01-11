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
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.MergedAnnotation;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wl4g.component.common.log.SmartLogger;

import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.endsWithAny;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.util.StringUtils.hasText;
import static com.wl4g.component.common.lang.ClassUtils2.isPresent;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.rpc.springboot.feign.annotation.EnableSpringBootFeignClients.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.Nullable;

/**
 * {@link SpringBootFeignClientsRegistrar}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
class SpringBootFeignClientsRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware {
	protected final SmartLogger log = getLogger(getClass());

	@SuppressWarnings("unused")
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
		if (hasSpringCloudFeignClass()) {
			log.info("The current classpath contains springcloud feign, "
					+ "which automatically enables the SpringCloud + Feign environment. "
					+ "SpringBoot + Feign has been ignored");
			return;
		}

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
		for (String pkg : (String[]) attrs.get(BASE_PACKAGES)) {
			if (hasText(pkg)) {
				scanBasePackages.add(pkg);
			}
		}
		for (Class<?> clazz : (Class[]) attrs.get(BASE_PACKAGE_CLASSES)) {
			scanBasePackages.add(ClassUtils.getPackageName(clazz));
		}
		if (scanBasePackages.isEmpty()) {
			scanBasePackages.add(ClassUtils.getPackageName(metadata.getClassName()));
		}
		return scanBasePackages;
	}

	public static boolean hasSpringCloudFeignClass() {
		return isPresent("org.springframework.cloud.openfeign.FeignClientsRegistrar");
	}

	/**
	 * {@link SpringBootFeignClientScanner}
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @version v1.0 2020-12-23
	 * @sine v1.0
	 * @see
	 */
	class SpringBootFeignClientScanner extends ClassPathBeanDefinitionScanner {

		@Nullable
		private final Class<?>[] defaultConfiguration;

		public SpringBootFeignClientScanner(BeanDefinitionRegistry registry, Class<?>[] defaultConfiguration) {
			super(registry, true);
			this.defaultConfiguration = defaultConfiguration;
			registerFilters();
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

		private void registerFilters() {
			// include service interfaces
			addIncludeFilter(new AnnotationTypeFilter(SpringBootFeignClient.class, true, true)); // [MARK1]
			// exclude package-info.java
			addExcludeFilter(new TypeFilter() {
				@Override
				public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory)
						throws IOException {
					String className = metadataReader.getClassMetadata().getClassName();
					return endsWithAny(className, "package-info", "module-info.java");
				}
			});
		}

		private String getRequestBaseUrl(MergedAnnotation<SpringBootFeignClient> feignClient) {
			if (feignClient.isPresent()) {
				return environment.resolveRequiredPlaceholders(feignClient.getString("url")); // base-URL
			}
			return "";
		}

		private String getRequestMappingPath(MergedAnnotation<RequestMapping> requestMapping) {
			if (requestMapping.isPresent()) {
				String[] paths = (String[]) requestMapping.getValue("value").get();
				if (nonNull(paths) && paths.length > 0) {
					// append to url suffix.
					return environment.resolveRequiredPlaceholders(paths[0]);
				}
			}
			return "";
		}

		@Override
		protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
			return beanDefinition.getMetadata().isInterface();
		}
	}

}
