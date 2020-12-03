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
package com.wl4g.components.core.web.versions.annotation;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.springframework.context.annotation.AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
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

import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.core.web.versions.reactive.ApiVersionRequestHandlerMapping;

import static com.wl4g.components.core.web.versions.annotation.EnableApiVersionMapping.VERSION_PARAMS;
import static com.wl4g.components.core.web.versions.annotation.EnableApiVersionMapping.GROUP_PARAMS;
import static com.wl4g.components.core.web.versions.annotation.EnableApiVersionMapping.VERSION_COMPARATOR;
import static com.wl4g.components.common.collection.Collections2.safeArrayToList;
import static com.wl4g.components.common.lang.Assert2.notNullOf;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.components.core.utils.context.SpringContextHolder.isReactiveWebApplication;

/**
 * {@link ApiVersionMappingRegistrar}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see
 */
public class ApiVersionMappingRegistrar
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
		AnnotationAttributes annoAttrs = AnnotationAttributes
				.fromMap(metadata.getAnnotationAttributes(EnableApiVersionMapping.class.getName()));
		if (!isNull(annoAttrs)) {
			BeanNameGenerator beanNameGenerator = resolveBeanNameGenerator(registry);

			// Register version comparator.
			registerVersionComparator(annoAttrs, registry, beanNameGenerator);

			// Register rqeust handler mapping.
			if (isReactiveWebApplication(getClass().getClassLoader(), environment, resourceLoader)) {
				registerRequestHandlerMapping(annoAttrs, registry, ApiVersionRequestHandlerMapping.class, beanNameGenerator);
			} else {
				registerRequestHandlerMapping(annoAttrs, registry,
						com.wl4g.components.core.web.versions.servlet.ApiVersionRequestHandlerMapping.class, beanNameGenerator);
			}
		}

	}

	protected void registerRequestHandlerMapping(AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry,
			Class<?> beanClass, BeanNameGenerator beanNameGenerator) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(beanClass);
		builder.addPropertyValue(VERSION_PARAMS, resolveVersionParameterNames(annoAttrs, registry));
		builder.addPropertyValue(GROUP_PARAMS, resolveVersionGroupParameterNames(annoAttrs, registry));
		builder.addPropertyValue(VERSION_COMPARATOR, beanFactory.getBean(annoAttrs.getClass(VERSION_COMPARATOR)));

		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
		String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
		registry.registerBeanDefinition(beanName, beanDefinition);
	}

	protected void registerVersionComparator(AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry,
			BeanNameGenerator beanNameGenerator) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(annoAttrs.getClass(VERSION_COMPARATOR));
		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();

		String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
		registry.registerBeanDefinition(beanName, beanDefinition);
	}

	private String[] resolveVersionParameterNames(AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry) {
		return safeArrayToList(annoAttrs.getStringArray(VERSION_PARAMS)).stream().filter(v -> !isBlank(v))
				.map(v -> environment.resolveRequiredPlaceholders(v)).toArray(String[]::new);
	}

	private String[] resolveVersionGroupParameterNames(AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry) {
		return safeArrayToList(annoAttrs.getStringArray(GROUP_PARAMS)).stream().filter(g -> !isBlank(g))
				.map(g -> environment.resolveRequiredPlaceholders(g)).toArray(String[]::new);
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
			log.warn(
					"BeanNameGenerator bean can't be found in BeanFactory with name [" + CONFIGURATION_BEAN_NAME_GENERATOR + "]");
			log.warn("BeanNameGenerator will be a instance of " + AnnotationBeanNameGenerator.class.getName()
					+ " , it maybe a potential problem on bean name generation.");
			beanNameGenerator = new AnnotationBeanNameGenerator();
		}
		return beanNameGenerator;
	}

}
