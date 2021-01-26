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
package com.wl4g.component.core.web.mapping.annotation;

import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.core.utils.context.SpringContextHolder.isServletWebApplication;
import static com.wl4g.component.core.web.mapping.annotation.EnableSmartMappingConfiguration.BASE_PACKAGES;
import static com.wl4g.component.core.web.mapping.annotation.EnableSmartMappingConfiguration.BASE_PACKAGE_CLASSES;
import static com.wl4g.component.core.web.mapping.annotation.EnableSmartMappingConfiguration.BASE_PACKAGES_FOR_INCLUDE;
import static com.wl4g.component.core.web.mapping.annotation.EnableSmartMappingConfiguration.FILTERS;
import static com.wl4g.component.core.web.mapping.annotation.EnableSmartMappingConfiguration.OVERRIDE_AMBIGUOUS;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.context.annotation.AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR;
import static org.springframework.util.StringUtils.hasText;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.SingletonBeanRegistry;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.AnnotationBeanNameGenerator;
import org.springframework.context.annotation.AnnotationConfigUtils;
import org.springframework.context.annotation.ConfigurationClassPostProcessor;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;

import com.google.common.base.Predicate;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.common.reflect.ObjectInstantiators;
import com.wl4g.component.core.web.mapping.WebFluxSmartHandlerMappingConfigurer;
import com.wl4g.component.core.web.mapping.WebMvcSmartHandlerMappingConfigurer;

/**
 * {@link SmartHandlerMappingRegistrar}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-17
 * @sine v1.0
 * @see
 */
public class SmartHandlerMappingRegistrar
		implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware, BeanFactoryAware {
	protected final SmartLogger log = getLogger(getClass());

	private ResourceLoader resourceLoader;
	private Environment environment;
	private BeanFactory beanFactory;

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = notNullOf(resourceLoader, "resourceLoader");
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = notNullOf(environment, "environment");
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = notNullOf(beanFactory, "beanFactory");
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		AnnotationAttributes attrs = AnnotationAttributes
				.fromMap(metadata.getAnnotationAttributes(EnableSmartMappingConfiguration.class.getName()));
		if (!isNull(attrs)) {
			BeanNameGenerator beanNameGenerator = resolveBeanNameGenerator(registry);

			// Register rqeust handler mapping.
			if (isServletWebApplication(getClass().getClassLoader(), (ConfigurableBeanFactory) beanFactory, environment,
					resourceLoader)) {
				registerSmartHandlerMappingConfigurer(metadata, attrs, registry, WebMvcSmartHandlerMappingConfigurer.class,
						beanNameGenerator);
			} else { // Webflux
				registerSmartHandlerMappingConfigurer(metadata, attrs, registry, WebFluxSmartHandlerMappingConfigurer.class,
						beanNameGenerator);
			}
		}
	}

	protected void registerSmartHandlerMappingConfigurer(AnnotationMetadata metadata, AnnotationAttributes attrs,
			BeanDefinitionRegistry registry, Class<?> beanClass, BeanNameGenerator beanNameGenerator) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);

		builder.addPropertyValue(BASE_PACKAGES, resolveBasePackages(metadata, attrs));
		builder.addPropertyValue(BASE_PACKAGES_FOR_INCLUDE, attrs.getBoolean(BASE_PACKAGES_FOR_INCLUDE));
		builder.addPropertyValue(FILTERS, resolveIncludeFilters(attrs, registry));
		builder.addPropertyValue(OVERRIDE_AMBIGUOUS, attrs.getBoolean(OVERRIDE_AMBIGUOUS));

		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
		String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
		registry.registerBeanDefinition(beanName, beanDefinition);
	}

	@SuppressWarnings("unchecked")
	private Predicate<Class<?>>[] resolveIncludeFilters(AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry) {
		return safeArrayToList(annoAttrs.getClassArray(FILTERS)).stream().map(c -> ObjectInstantiators.newInstance(c))
				.toArray(Predicate[]::new);
	}

	/**
	 * It'd better to use BeanNameGenerator instance that should reference
	 * {@link ConfigurationClassPostProcessor#componentScanBeanNameGenerator},
	 * thus it maybe a potential problem on bean name generation.
	 *
	 * @param registry
	 *            {@link BeanDefinitionRegistry}
	 * @return {@link BeanNameGenerator} instance
	 * @see SingletonBeanRegistry
	 * @see AnnotationConfigUtils#CONFIGURATION_BEAN_NAME_GENERATOR
	 * @see ConfigurationClassPostProcessor#processConfigBeanDefinitions
	 * @since 2.5.8
	 */
	private BeanNameGenerator resolveBeanNameGenerator(BeanDefinitionRegistry registry) {
		BeanNameGenerator beanNameGenerator = null;
		if (registry instanceof SingletonBeanRegistry) {
			SingletonBeanRegistry singletonBeanRegistry = SingletonBeanRegistry.class.cast(registry);
			beanNameGenerator = (BeanNameGenerator) singletonBeanRegistry.getSingleton(CONFIGURATION_BEAN_NAME_GENERATOR);
		}
		if (beanNameGenerator == null) {
			log.warn("BeanNameGenerator bean can't be found in BeanFactory with name [" + CONFIGURATION_BEAN_NAME_GENERATOR
					+ "], BeanNameGenerator will be a instance of " + AnnotationBeanNameGenerator.class.getName()
					+ " , it maybe a potential problem on bean name generation.");
			beanNameGenerator = new AnnotationBeanNameGenerator();
		}
		return beanNameGenerator;
	}

	private String[] resolveBasePackages(AnnotationMetadata metadata, AnnotationAttributes attrs) {
		return getBasePackages(metadata, attrs).stream().filter(v -> !isBlank(v))
				.map(v -> environment.resolveRequiredPlaceholders(v)).toArray(String[]::new);
	}

	private Set<String> getBasePackages(AnnotationMetadata metadata, AnnotationAttributes attrs) {
		Set<String> basePackages = new HashSet<>();
		for (String pkg : (String[]) attrs.get("value")) {
			if (hasText(pkg)) {
				basePackages.add(pkg);
			}
		}
		for (String pkg : (String[]) attrs.get(BASE_PACKAGES)) {
			if (hasText(pkg)) {
				basePackages.add(pkg);
			}
		}
		for (Class<?> clazz : (Class[]) attrs.get(BASE_PACKAGE_CLASSES)) {
			basePackages.add(ClassUtils.getPackageName(clazz));
		}
		if (basePackages.isEmpty()) {
			basePackages.add(ClassUtils.getPackageName(metadata.getClassName()));
		}
		return basePackages;
	}

}
