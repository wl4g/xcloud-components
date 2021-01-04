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

import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.component.common.lang.StringUtils2.isTrue;
import static java.lang.System.getProperty;
import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.springframework.boot.context.config.ConfigFileApplicationListener.CONFIG_LOCATION_PROPERTY;
import static org.springframework.boot.context.config.ConfigFileApplicationListener.CONFIG_ADDITIONAL_LOCATION_PROPERTY;

import java.util.Properties;

import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/**
 * Application starting defaults configuration processing listener.</br>
 * refer: {@link LoggingApplicationListener} implements.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年5月20日
 * @since
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 9)
public class DefaultConfigurationApplicationListener extends AbstractStartingApplicationListener {

	/**
	 * Setup defaults configuration properties.
	 * 
	 * @param event
	 */
	@Override
	public void onStarting(ApplicationStartingEvent event) throws Exception {
		Properties defaultProperties = new Properties();
		defaultProperties.put("spring.main.allow-bean-definition-overriding", "true");

		// Addidtion default config location.
		String argsString = safeArrayToList(event.getArgs()).toString();
		if (!containsAny(argsString, CONFIG_ADDITIONAL_LOCATION_PROPERTY, CONFIG_LOCATION_PROPERTY)
				&& !isTrue(getProperty("disable.default-config-location"), false)) {
			defaultProperties.put(CONFIG_ADDITIONAL_LOCATION_PROPERTY, DEFAULT_CONFIG_ADD_LOCATION);
		}
		event.getSpringApplication().setDefaultProperties(defaultProperties);
	}

	public static final String DEFAULT_CONFIG_ADD_LOCATION = "classpath:/,classpath:/sbf/,classpath:/scf/,classpath:/dubbo/";

}