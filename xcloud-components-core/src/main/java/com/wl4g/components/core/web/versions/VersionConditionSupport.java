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
package com.wl4g.components.core.web.versions;

import static com.wl4g.components.common.lang.Assert2.notNullOf;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.util.Objects.isNull;

import java.util.Comparator;

import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.core.web.versions.annotation.ApiVersion;
import com.wl4g.components.core.web.versions.annotation.ApiVersionGroup;

/**
 * {@link VersionConditionSupport}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see
 */
public abstract class VersionConditionSupport {
	protected final SmartLogger log = getLogger(getClass());

	protected final ApiVersionGroup apiVersionGroup;
	protected final ApiVersion apiVersion;
	protected final Comparator<String> versionComparator;

	public VersionConditionSupport(ApiVersionGroup apiVersionGroup, ApiVersion apiVersion, Comparator<String> versionComparator) {
		this.apiVersionGroup = apiVersionGroup;
		this.apiVersion = apiVersion;
		this.versionComparator = notNullOf(versionComparator, "versionComparator");
		// Validation versions
		if (isNull(apiVersionGroup) && isNull(apiVersion)) {
			throw new IllegalStateException(format("Annotations %s and %s should not be null at the same time.",
					ApiVersionGroup.class.getSimpleName(), ApiVersion.class.getSimpleName()));
		}
	}

	protected ApiVersionGroup getApiVersionGroup() {
		return apiVersionGroup;
	}

	protected ApiVersion getApiVersion() {
		return apiVersion;
	}

}
