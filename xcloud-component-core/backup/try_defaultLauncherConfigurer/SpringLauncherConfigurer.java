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

import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;

import java.util.Properties;

import com.wl4g.component.common.log.SmartLogger;

/**
 * {@link SpringLauncherConfigurer}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-07
 * @sine v1.0
 * @see
 */
public abstract class SpringLauncherConfigurer {
	protected final SmartLogger log = getLogger(getClass());

	protected final String configLocation;

	protected SpringLauncherConfigurer(String configLocation) {
		this.configLocation = hasTextOf(configLocation, "configLocation");
	}

	/**
	 * Resolve generate spring application additional profiles.
	 * 
	 * @return
	 */
	protected String[] resolveAdditionalProfiles() {
		return null;
	}

	/**
	 * Resolve generate spring application default properties.
	 * 
	 * @return
	 */
	protected Properties resolveDefaultProperties() {
		return null;
	}

}
