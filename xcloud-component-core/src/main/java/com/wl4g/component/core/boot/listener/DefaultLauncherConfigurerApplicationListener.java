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
package com.wl4g.component.core.boot.listener;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.component.common.collection.CollectionUtils2.safeArray;
import static com.wl4g.component.common.lang.StringUtils2.isTrue;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.lang.System.getProperty;
import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.replaceEach;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;

import org.codehaus.groovy.control.CompilationFailedException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigFileApplicationListener;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ReflectionUtils;

import com.google.common.io.Resources;
import com.wl4g.component.common.collection.CollectionUtils2;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.common.resource.resolver.ClassPathResourcePatternResolver;

import groovy.lang.GroovyClassLoader;

/**
 * Config bootstrap application listener. Before executing
 * {@link ConfigFileApplicationListener}, in order to set the boot
 * configuration.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年5月20日
 * @since
 */
public class DefaultLauncherConfigurerApplicationListener implements GenericApplicationListener {
	protected final SmartLogger log = getLogger(getClass());

	// Notes: If you need to customize boot configuration (override this kind of
	// logic), please inherit this class and rewrite this method, and set the
	// return value to be larger.
	//
	// 注：如果需要自定义启动引导配置（覆盖此类逻辑），请继承此类并重写此方法，设置返回值大于此值即可
	@Override
	public int getOrder() {
		return DEFAULT_ORDER;
	}

	@Override
	public boolean supportsEventType(ResolvableType resolvableType) {
		return isAssignableFrom(resolvableType.getRawClass(), ApplicationStartingEvent.class);
	}

	/**
	 * Refer to {@link LoggingApplicationListener} implemention
	 */
	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return isAssignableFrom(sourceType, SpringApplication.class, ApplicationContext.class);
	}

	/**
	 * Refer to: </br>
	 * {@link org.springframework.boot.SpringApplication#run(String)} and
	 * {@link org.springframework.boot.SpringApplicationRunListeners#starting()}
	 */
	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationStartingEvent) {
			try {
				ApplicationStartingEvent starting = (ApplicationStartingEvent) event;
				presetSpringApplication(starting, starting.getSpringApplication());
			} catch (Exception e) {
				throw new IllegalStateException("Cannot preset SpringApplication properties", e);
			}
		}
	}

	/**
	 * Preset {@link SpringApplication} properties.
	 * 
	 * @param event
	 * @param application
	 * @throws Exception
	 */
	protected void presetSpringApplication(ApplicationStartingEvent event, SpringApplication application) throws Exception {
		if (isTrue(getProperty("disable.launcher-configurer"), false)) { // Skip?
			return;
		}
		// Create launcher configurer.
		ISpringLauncherConfigurer configurer = loadClassAndInstantiateSpringLauncherConfigurer();
		if (nonNull(configurer)) {
			presetDefaultProperties(event, application, configurer);
			presetAdditionalProfiles(event, application, configurer);
			presetOtherProperties(event, application, configurer);
		}
	}

	protected void presetDefaultProperties(ApplicationStartingEvent event, SpringApplication application,
			ISpringLauncherConfigurer configurer) throws Exception {
		Properties defaultProperties = new Properties();
		// defaultProperties.put("spring.main.allow-bean-definition-overriding",
		// "true");
		if (nonNull(configurer.defaultProperties())) {
			defaultProperties.putAll(configurer.defaultProperties());
		}

		// Command-line arguments are preferred.
		ApplicationArguments args = new DefaultApplicationArguments(event.getArgs());
		for (String argName : args.getOptionNames()) {
			List<String> values = args.getOptionValues(argName);
			if (!CollectionUtils.isEmpty(values)) {
				defaultProperties.put(argName, values);
			}
		}

		log.info("Preset SpringApplication default properties: {}", defaultProperties);
		application.setDefaultProperties(defaultProperties);
	}

	protected void presetAdditionalProfiles(ApplicationStartingEvent event, SpringApplication application,
			ISpringLauncherConfigurer configurer) throws Exception {
		if (nonNull(configurer.additionalProfiles())) {
			application.setAdditionalProfiles(configurer.additionalProfiles());
		}
	}

	protected void presetOtherProperties(ApplicationStartingEvent event, SpringApplication application,
			ISpringLauncherConfigurer configurer) throws Exception {
		if (nonNull(configurer.allowBeanDefinitionOverriding())) {
			application.setAllowBeanDefinitionOverriding(configurer.allowBeanDefinitionOverriding());
		}
		if (nonNull(configurer.addCommandLineProperties())) {
			application.setAddCommandLineProperties(configurer.addCommandLineProperties());
		}
	}

	/**
	 * Resolve launcher class and instantiate.
	 * 
	 * @return
	 * @throws Exception
	 */
	private ISpringLauncherConfigurer loadClassAndInstantiateSpringLauncherConfigurer() throws Exception {
		// Load launcher classes.
		List<Class<? extends ISpringLauncherConfigurer>> classes = emptyList();
		try (GroovyClassLoader gcl = new GroovyClassLoader()) {
			ClassPathResourcePatternResolver resolver = new ClassPathResourcePatternResolver();
			classes = resolver.getResources(DEFAULT_LAUNCHER_CLASSNAME).stream().map(r -> {
				try {
					return gcl.parseClass(Resources.toString(r.getURL(), UTF_8),
							defaultClassNameConverter.apply(r.getFilename()));
				} catch (CompilationFailedException | IOException e) {
					throw new IllegalStateException(e);
				}
			}).collect(toList());
		}

		if (!CollectionUtils2.isEmpty(classes)) {
			AnnotationAwareOrderComparator.sort(classes);
			Class<? extends ISpringLauncherConfigurer> bestClass = classes.get(0);
			return ReflectionUtils.accessibleConstructor(bestClass).newInstance();
		}

		return null;
	}

	/**
	 * Check type is assignable from supportedTypes
	 * 
	 * @param type
	 * @param supportedTypes
	 * @return
	 */
	private boolean isAssignableFrom(Class<?> type, Class<?>... supportedTypes) {
		if (type != null) {
			for (Class<?> supportedType : safeArray(Class.class, supportedTypes)) {
				if (supportedType.isAssignableFrom(type)) {
					return true;
				}
			}
		}
		return false;
	}

	private static final Function<String, String> defaultClassNameConverter = filename -> replaceEach(filename,
			new String[] { "!", "@", "#", "-", "&", "*" }, new String[] { "_", "_", "_", "_", "_", "_" });

	public static final int DEFAULT_ORDER = Ordered.HIGHEST_PRECEDENCE + 5;
	public static final String DEFAULT_LAUNCHER_CLASSNAME = "classpath*:/META-INF/default-launcher.groovy";
}