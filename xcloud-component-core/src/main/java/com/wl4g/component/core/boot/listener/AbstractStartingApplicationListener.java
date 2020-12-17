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

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationStartingEvent;
import org.springframework.boot.context.logging.LoggingApplicationListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.GenericApplicationListener;
import org.springframework.core.ResolvableType;

import com.wl4g.component.common.log.SmartLogger;

/**
 * Application starting event processing listener.</br>
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年5月20日
 * @since
 */
public abstract class AbstractStartingApplicationListener implements GenericApplicationListener {

	protected final SmartLogger log = getLogger(getClass());

	@Override
	public boolean supportsEventType(ResolvableType resolvableType) {
		return isAssignableFrom(resolvableType.getRawClass(), EVENT_TYPES);
	}

	@Override
	public boolean supportsSourceType(Class<?> sourceType) {
		return isAssignableFrom(sourceType, SOURCE_TYPES);
	}

	@Override
	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof ApplicationStartingEvent) {
			try {
				onStarting((ApplicationStartingEvent) event);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}

	/**
	 * On starting event configuration processing.
	 * 
	 * @param event
	 * @throws Exception
	 */
	protected abstract void onStarting(ApplicationStartingEvent event) throws Exception;

	/**
	 * Check type is assignable from supportedTypes
	 * 
	 * @param type
	 * @param supportedTypes
	 * @return
	 */
	private boolean isAssignableFrom(Class<?> type, Class<?>... supportedTypes) {
		if (type != null) {
			for (Class<?> supportedType : supportedTypes) {
				if (supportedType.isAssignableFrom(type)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Match supports events type. </br>
	 * 
	 * refer: {@link LoggingApplicationListener#EVENT_TYPES} implements.
	 */
	final private static Class<?>[] EVENT_TYPES = { ApplicationStartingEvent.class };
	final private static Class<?>[] SOURCE_TYPES = { SpringApplication.class, ApplicationContext.class };

}