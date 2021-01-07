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
package com.wl4g.component.core.boot.listener;

import java.util.Collection;
import java.util.Properties;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;

/**
 * {@link ISpringLauncherConfigurer}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-07
 * @sine v1.0
 * @see
 */
public interface ISpringLauncherConfigurer extends Ordered {

	@Override
	default int getOrder() {
		return 0; // Default ordered
	}

	/**
	 * Resolve for preset {@link SpringApplication#setHeadless(boolean)}
	 * 
	 * @return
	 */
	default Boolean headless() {
		return null;
	}

	/**
	 * Resolve for preset {@link SpringApplication#setBanner(Banner)}
	 * 
	 * @return
	 */
	default Banner banner() {
		return null;
	}

	/**
	 * Resolve for preset {@link SpringApplication#setBannerMode(Banner.Mode)}
	 * 
	 * @return
	 */
	default Banner.Mode bannerMode() {
		return null;
	}

	/**
	 * Resolve for preset {@link SpringApplication#setLogStartupInfo(boolean)}
	 * 
	 * @return
	 */
	default Boolean logStartupInfo() {
		return null;
	}

	/**
	 * Resolve for preset
	 * {@link SpringApplication#setAddCommandLineProperties(boolean)}
	 * 
	 * @return
	 */
	default Boolean addCommandLineProperties() {
		return null;
	}

	/**
	 * Resolve for preset
	 * {@link SpringApplication#setApplicationContextClass(Class)}
	 * 
	 * @return
	 */
	default Class<? extends ConfigurableApplicationContext> applicationContextClass() {
		return null;
	}

	/**
	 * Resolve for preset
	 * {@link SpringApplication#setInitializers(java.util.Collection)}
	 * 
	 * @return
	 */
	default Collection<? extends ApplicationContextInitializer<?>> initializers() {
		return null;
	}

	/**
	 * Resolve for preset {@link SpringApplication#setListeners(Collection)}
	 * 
	 * @return
	 */
	default Collection<? extends ApplicationListener<?>> listeners() {
		return null;
	}

	/**
	 * Resolve for preset {@link SpringApplication#addListeners(Collection)}
	 * 
	 * @return
	 */
	default ApplicationListener<?>[] addListeners() {
		return null;
	}

	/**
	 * Resolve for preset
	 * {@link SpringApplication#setLazyInitialization(boolean)}
	 * 
	 * @return
	 */
	default Boolean lazyInitialization() {
		return null;
	}

	/**
	 * Resolve for preset
	 * {@link SpringApplication#setAllowBeanDefinitionOverriding(boolean)}
	 * 
	 * @return
	 */
	default Boolean allowBeanDefinitionOverriding() {
		// In order to encapsulate the upper frame, it must be opened.
		return true;
	}

	/**
	 * Resolve for preset
	 * {@link SpringApplication#setAdditionalProfiles(String...)}
	 * 
	 * @return
	 */
	default String[] additionalProfiles() {
		return null;
	}

	/**
	 * Resolve for preset
	 * {@link SpringApplication#setDefaultProperties(Properties)}
	 * 
	 * @return
	 */
	default Properties defaultProperties() {
		return null;
	}

}
