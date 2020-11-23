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

import java.util.List;
import java.util.Set;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

import com.alibaba.dubbo.config.spring.ServiceBean;

import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.filter.TypeFilter;
import static org.springframework.util.Assert.notNull;

import com.wl4g.components.common.lang.Assert2;
import static com.wl4g.components.common.collection.Collections2.safeMap;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.components.rpc.springcloud.util.FeignDubboUtils.generateFeignProxyBeanName;
import static com.wl4g.components.core.utils.AopUtils2.isCglibProxy;

/**
 * The scanning injection is realized with reference to
 * {@link org.mybatis.spring.mapper.MapperScannerConfigurer},
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-23
 * @sine v1.0
 * @see
 */
public class FeignProviderProxiesConfigurer
		implements BeanDefinitionRegistryPostProcessor, InitializingBean, ApplicationContextAware, BeanNameAware {

	protected final Logger log = getLogger(getClass());

	private ApplicationContext applicationContext;
	@SuppressWarnings("unused")
	private String beanName;
	private TypeFilter includeFilter;
	private Class<?> superClass;
	private Set<String> basePackages;
	@SuppressWarnings("unused")
	@Deprecated
	private ResourceLoader resourceLoader;
	private Environment environment;
	private DefaultListableBeanFactory beanFactory;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public void setIncludeFilter(TypeFilter includeFilter) {
		this.includeFilter = includeFilter;
	}

	public void setSuperClass(Class<?> superClass) {
		this.superClass = superClass;
	}

	public void setBasePackages(Set<String> basePackages) {
		this.basePackages = basePackages;
	}

	public void setResourceLoader(ResourceLoader resourceLoader) {
		this.resourceLoader = resourceLoader;
	}

	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	public void setBeanFactory(DefaultListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		notNull(this.basePackages, "Property 'basePackage' is required");
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		// left intentionally blank
	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		ClassPathScanningCandidateComponentProvider scanner = getScanner();
		// scanner.setResourceLoader(resourceLoader);
		scanner.setResourceLoader(applicationContext);
		scanner.addIncludeFilter(includeFilter);

		for (String basePackage : basePackages) {
			Set<BeanDefinition> candidateComponents = scanner.findCandidateComponents(basePackage);
			for (BeanDefinition candidateComponent : candidateComponents) {
				if (candidateComponent instanceof AnnotatedBeanDefinition) {
					// verify annotated class is an interface
					AnnotatedBeanDefinition beanDefinition = (AnnotatedBeanDefinition) candidateComponent;
					AnnotationMetadata annotationMetadata = beanDefinition.getMetadata();
					Assert2.isTrue(annotationMetadata.isInterface(), "@FeignClient can only be specified on an interface");

					Class<?> interfaceClass = null;
					try {
						interfaceClass = Class.forName(beanDefinition.getBeanClassName());
					} catch (ClassNotFoundException e) {
						log.warn("Not found class : '{}'", beanDefinition.getBeanClassName());
					}

					// Proxy Feign Client
					if (interfaceClass != null) {
						// Obtain orig bean instance.
						Object bean = null;
						try {
							bean = beanFactory.getBean(interfaceClass);
						} catch (NoUniqueBeanDefinitionException e) {
							// Fallback, find the original object from multiple
							// beans.
							List<Object> candidateBeans = safeMap(beanFactory.getBeansOfType(interfaceClass)).values().stream()
									.filter(obj -> !isNull(obj) && !(obj instanceof ServiceBean) && !isCglibProxy(obj))
									.collect(toList());
							if (candidateBeans.size() == 1) {
								bean = candidateBeans.get(0);
							} else {
								throw e;
							}
						}

						if (bean != null) {
							Enhancer enhancer = new Enhancer();
							if (superClass != null) {
								enhancer.setSuperclass(superClass);
							}
							enhancer.setCallback(new FeignProxyInvocationHandler(bean));
							enhancer.setInterfaces(new Class[] { interfaceClass, FeignProxyController.class });
							Object proxy = enhancer.create();
							beanFactory.registerSingleton(generateFeignProxyBeanName(interfaceClass.getName()), proxy);
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

}
