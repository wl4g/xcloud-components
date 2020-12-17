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
package com.wl4g.components.core.config.listener;

import static java.util.Objects.isNull;

import java.io.IOException;
import java.lang.reflect.Field;

import org.springframework.beans.factory.config.YamlMapFactoryBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import static org.springframework.util.ReflectionUtils.findField;
import static org.springframework.util.ReflectionUtils.getField;
import static org.springframework.util.ReflectionUtils.setField;
import static org.springframework.util.ReflectionUtils.makeAccessible;

import static org.apache.commons.lang3.StringUtils.equalsAnyIgnoreCase;
import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.components.common.collection.Collections2.safeArrayToList;
import static com.wl4g.components.common.collection.Collections2.safeSet;
import static com.wl4g.components.common.lang.StringUtils2.getFilenameExtension;
import static com.wl4g.components.common.io.ByteStreamUtils.readFullyToString;
import com.wl4g.components.core.annotation.EnableMergingAutoConfiguration;

/**
 * Application listener for shared configuration overlay merge.</br>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-16
 * @sine v1.0
 * @see
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class MergingConfigurationApplicationListener extends AbstractStartingApplicationListener {

	@SuppressWarnings("unchecked")
	@Override
	public void onStarting(ApplicationStartingEvent event) throws Exception {
		SpringApplication app = event.getSpringApplication();

		EnableMergingAutoConfiguration merging = safeSet(app.getAllSources()).stream()
				.map(s -> ((Class<EnableMergingAutoConfiguration>) s).getAnnotation(EnableMergingAutoConfiguration.class))
				.filter(s -> !isNull(s)).findAny().orElse(null);
		if (isNull(merging)) {
			log.debug("No enable auto merging environment configuration.");
			return;
		}

		// Loading share configuration & merging
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		for (String location : merging.locations()) {
			for (Resource res : safeArrayToList(resolver.getResources(location))) {
				processMergingConfiguration(app, res);
			}
		}

	}

	/**
	 * Merging share configuration environment.
	 * 
	 * @param application
	 * @param mergingResource
	 * @throws IOException
	 */
	private void processMergingConfiguration(SpringApplication application, Resource mergingResource) throws IOException {
		if (!equalsAnyIgnoreCase(getFilenameExtension(mergingResource.getFilename()), "yml", "yaml")) {
			log.debug("Skip auto merging environment configuration. - {}", mergingResource);
			return;
		}

		// Gets olders configurable environment.
		Field envField = findField(application.getClass(), "environment", ConfigurableEnvironment.class);
		makeAccessible(envField);
		ConfigurableEnvironment env = (ConfigurableEnvironment) getField(envField, application);

		// Do merging
		doMerging(env, readFullyToString(mergingResource.getInputStream()));

		// Sets newer configurable environment.
		setField(envField, application, env);
	}

	/**
	 * Do merging share configuration environment.
	 * 
	 * @param env
	 * @param content
	 */
	private void doMerging(ConfigurableEnvironment env, String content) {
		// // Merging actives.(overlay)
		// List<String> profiles = safeArrayToList(env.getActiveProfiles());
		// env.setActiveProfiles(profiles.toArray(new String[0]));

		// Merging configuration.(overlay)
		YamlMapFactoryBean factory = new YamlMapFactoryBean();
		factory.setResources(new ByteArrayResource(content.getBytes(UTF_8)));
		factory.afterPropertiesSet();

		MutablePropertySources sources = env.getPropertySources();
		sources.forEach(s -> {

		});

	}

}