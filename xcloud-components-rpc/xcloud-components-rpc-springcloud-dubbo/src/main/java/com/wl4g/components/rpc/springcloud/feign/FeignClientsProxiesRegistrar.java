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
package com.wl4g.components.rpc.springcloud.feign;

import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.AbstractClassTestingTypeFilter;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.TypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import static org.springframework.util.StringUtils.*;

import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.*;

/**
 * Patterned after Spring Integration IntegrationComponentScanRegistrar and
 * RibbonClientsConfigurationRegistgrar.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author Spencer Gibb
 * @author Jakub Narloch
 * @author Venil Noronha
 * @author Gang Li
 * @version v1.0 2019-11-20
 * @sine v1.0
 * @see Implementation of refer:
 *      {@link org.springframework.cloud.openfeign.FeignClientsRegistrar}
 */
class FeignClientsProxiesRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware, EnvironmentAware, BeanFactoryAware {

	protected final Logger log = getLogger(getClass());
	private ResourceLoader resourceLoader;
	private Environment environment;
	private BeanFactory beanFactory;

	@Override
	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	public void registerBeanDefinitions(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		registerFeignClients(metadata, registry);
	}

	public void registerFeignClients(AnnotationMetadata metadata, BeanDefinitionRegistry registry) {
		ClassPathScanningCandidateComponentProvider scanner = getScanner();
		scanner.setResourceLoader(this.resourceLoader);

		Set<String> basePackages;
		Map<String, Object> attrs = metadata.getAnnotationAttributes(EnableFeignClientsProxies.class.getName());

		AnnotationTypeFilter annotationTypeFilter = new AnnotationTypeFilter(FeignClient.class);
		final Class<?>[] clients = attrs == null ? null : (Class<?>[]) attrs.get("clients");

		Class<?> superClass = attrs == null ? null : (Class<?>) attrs.get("superClass");
		superClass = superClass.equals(Void.class) ? null : superClass;

		if (clients == null || clients.length == 0) {
			scanner.addIncludeFilter(annotationTypeFilter);
			basePackages = getBasePackages(metadata);
		} else {
			final Set<String> clientClasses = new HashSet<>();
			basePackages = new HashSet<>();
			for (Class<?> clazz : clients) {
				basePackages.add(ClassUtils.getPackageName(clazz));
				clientClasses.add(clazz.getCanonicalName());
			}
			AbstractClassTestingTypeFilter filter = new AbstractClassTestingTypeFilter() {
				@Override
				protected boolean match(ClassMetadata metadata) {
					String cleaned = metadata.getClassName().replaceAll("\\$", ".");
					return clientClasses.contains(cleaned);
				}
			};
			scanner.addIncludeFilter(new AllTypeFilter(asList(filter, annotationTypeFilter)));
		}

		DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) beanFactory;

		for (String basePackage : basePackages) {
			Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
			for (BeanDefinition candidateComponent : candidateComponents) {
				if (candidateComponent instanceof AnnotatedBeanDefinition) {
					// verify annotated class is an interface
					AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
					AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
					Assert.isTrue(annotationMetadata.isInterface(), "@FeignClient can only be specified on an interface");

					Class<?> interfaceClass = null;
					try {
						interfaceClass = Class.forName(beanDefinition.getBeanClassName());
					} catch (ClassNotFoundException e) {
						log.warn("Not found class : '{}'", beanDefinition.getBeanClassName());
					}

					// Proxy Feign Client
					if (interfaceClass != null) {
						Object bean = beanFactory.getBean(interfaceClass);
						if (bean != null) {
							Enhancer enhancer = new Enhancer();
							if (superClass != null) {
								enhancer.setSuperclass(superClass);
							}
							enhancer.setCallback(new FeignProxyInvocationHandler(bean));
							enhancer.setInterfaces(new Class[] { interfaceClass, FeignProxyController.class });
							Object proxy = enhancer.create();
							defaultListableBeanFactory.registerSingleton(
									capitalize(interfaceClass.getSimpleName()) + BEAN_FEIGNPROXY_SUFFIX, proxy);
							log.debug("Feign client {} proxy by {}", interfaceClass, proxy);
						} else {
							log.debug("Feign client {} no implementor founded", interfaceClass);
						}
					}
				}
			}
		}
	}

	protected ClassPathScanningCandidateComponentProvider getScanner() {
		return new ClassPathScanningCandidateComponentProvider(false, this.environment) {
			@Override
			protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
				boolean isCandidate = false;
				if (beanDefinition.getMetadata().isIndependent()) {
					if (!beanDefinition.getMetadata().isAnnotation()) {
						isCandidate = true;
					}
				}
				return isCandidate;
			}
		};
	}

	protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
		Map<String, Object> attributes = importingClassMetadata
				.getAnnotationAttributes(EnableFeignClientsProxies.class.getCanonicalName());

		Set<String> basePackages = new HashSet<>();
		for (String pkg : (String[]) attributes.get("value")) {
			if (hasText(pkg)) {
				basePackages.add(pkg);
			}
		}
		for (String pkg : (String[]) attributes.get("basePackages")) {
			if (hasText(pkg)) {
				basePackages.add(pkg);
			}
		}
		for (Class<?> clazz : (Class[]) attributes.get("basePackageClasses")) {
			basePackages.add(ClassUtils.getPackageName(clazz));
		}

		if (basePackages.isEmpty()) {
			basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
		}
		return basePackages;
	}

	/**
	 * Helper class to create a {@link TypeFilter} that matches if all the
	 * delegates match.
	 *
	 * @author Oliver Gierke
	 */
	private static class AllTypeFilter implements TypeFilter {

		private final List<TypeFilter> delegates;

		/**
		 * Creates a new {@link AllTypeFilter} to match if all the given
		 * delegates match.
		 *
		 * @param delegates
		 *            must not be {@literal null}.
		 */
		public AllTypeFilter(List<TypeFilter> delegates) {
			Assert.notNull(delegates, "This argument is required, it must not be null");
			this.delegates = delegates;
		}

		@Override
		public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) throws IOException {
			for (TypeFilter filter : this.delegates) {
				if (!filter.match(metadataReader, metadataReaderFactory)) {
					return false;
				}
			}
			return true;
		}

	}

	public static final String BEAN_FEIGNPROXY_SUFFIX = "$".concat(FeignProxyController.class.getSimpleName());

}