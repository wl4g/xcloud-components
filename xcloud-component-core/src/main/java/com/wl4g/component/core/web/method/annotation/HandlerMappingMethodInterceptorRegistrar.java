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
package com.wl4g.component.core.web.method.annotation;

import static com.wl4g.component.common.collection.CollectionUtils2.isEmptyArray;
import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.lang.ClassUtils2.getPackageName;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.core.utils.context.SpringContextHolder.isServletWebApplication;
import static com.wl4g.component.core.web.mapping.annotation.EnableSmartMappingConfiguration.BASE_PACKAGES;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isInterface;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.context.annotation.AnnotationConfigUtils.CONFIGURATION_BEAN_NAME_GENERATOR;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.support.AbstractGenericPointcutAdvisor;
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
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.startsWithAny;
import static org.springframework.beans.factory.support.BeanDefinitionBuilder.genericBeanDefinition;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.core.web.method.HandlerMethodCustomizerInterceptor;

/**
 * {@link HandlerMappingMethodInterceptorRegistrar}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-17
 * @sine v1.0
 * @see
 */
public class HandlerMappingMethodInterceptorRegistrar
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
				.fromMap(metadata.getAnnotationAttributes(EnableHandlerMappingCustomizer.class.getName()));
		if (!isNull(annoAttrs)) {
			BeanNameGenerator beanNameGenerator = resolveBeanNameGenerator(registry);

			if (isServletWebApplication(getClass().getClassLoader(), beanFactory, environment, resourceLoader)) {
				// register post conversion interceptor.
				BeanDefinitionBuilder builder1 = genericBeanDefinition(HandlerMethodCustomizerInterceptor.class);
				builder1.addPropertyValue("beanFactory", beanFactory);
				registerBeanDefinition(builder1, registry, beanNameGenerator);

				// register post conversion interceptor advisor.
				BeanDefinitionBuilder builder2 = genericBeanDefinition(HumanModelConvertAdvisor.class);
				builder2.addPropertyValue("scanBasePackages", resolveScanBasePackages(annoAttrs, registry));
				builder2.addPropertyValue("advice", beanFactory.getBean(HandlerMethodCustomizerInterceptor.class));

				registerBeanDefinition(builder2, registry, beanNameGenerator);
			} else { // Webflux
				// TODO
			}
		}
	}

	protected void registerBeanDefinition(BeanDefinitionBuilder builder, BeanDefinitionRegistry registry,
			BeanNameGenerator beanNameGenerator) {

		AbstractBeanDefinition beanDefinition = builder.getBeanDefinition();
		String beanName = beanNameGenerator.generateBeanName(beanDefinition, registry);
		registry.registerBeanDefinition(beanName, beanDefinition);
	}

	private String[] resolveScanBasePackages(AnnotationAttributes annoAttrs, BeanDefinitionRegistry registry) {
		return safeArrayToList(annoAttrs.getStringArray(BASE_PACKAGES)).stream().filter(v -> !isBlank(v))
				.map(v -> environment.resolveRequiredPlaceholders(v)).toArray(String[]::new);
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

	/**
	 * AOP advisor of {@link HandlerMethodCustomizerInterceptor}
	 */
	static class HumanModelConvertAdvisor extends AbstractGenericPointcutAdvisor {
		final private static long serialVersionUID = 1L;

		private String[] scanBasePackages;

		public void setScanBasePackages(String[] scanBasePackages) {
			this.scanBasePackages = scanBasePackages;
		}

		@Override
		public Pointcut getPointcut() {
			return new Pointcut() {
				final private List<String> EXCLUDE_METHODS = new ArrayList<String>(4) {
					private static final long serialVersionUID = 3369346948736795743L;
					{
						addAll(asList(Object.class.getDeclaredMethods()).stream().map(m -> m.getName()).collect(toList()));
					}
				};

				@Override
				public MethodMatcher getMethodMatcher() {
					return new MethodMatcher() {

						@Override
						public boolean matches(Method method, Class<?> targetClass) {
							boolean hasRequestMapping = hasAnnotation(method, RequestMapping.class);
							int mod = method.getModifiers();
							String name = method.getName();
							return hasRequestMapping && !isAbstract(mod) && isPublic(mod)
									&& !isInterface(method.getDeclaringClass().getModifiers()) && !EXCLUDE_METHODS.contains(name);
						}

						@Override
						public boolean isRuntime() {
							return false;
						}

						@Override
						public boolean matches(Method method, Class<?> targetClass, Object... args) {
							throw new Error("Shouldn't be here");
						}
					};
				}

				@Override
				public ClassFilter getClassFilter() {
					return clazz -> (hasAnnotation(clazz, ResponseBody.class)
							&& (isEmptyArray(scanBasePackages) || startsWithAny(getPackageName(clazz), scanBasePackages))
							&& (hasAnnotation(clazz, Controller.class) || hasAnnotation(clazz, RequestMapping.class)));
				}
			};
		}

	}

}
