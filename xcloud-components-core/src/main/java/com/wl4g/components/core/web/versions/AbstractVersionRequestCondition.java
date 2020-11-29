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

import static com.wl4g.components.common.lang.Assert2.isTrue;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.util.Objects.isNull;

import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.core.web.versions.annotation.ApiVersion;
import com.wl4g.components.core.web.versions.annotation.MultiApiVersion;

/**
 * {@link AbstractVersionRequestCondition}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see
 */
public abstract class AbstractVersionRequestCondition {

	protected final SmartLogger log = getLogger(getClass());

	protected final MultiApiVersion multiApiVersion;
	protected final ApiVersion apiVersion;

	protected final Integer combineVersion;

	public AbstractVersionRequestCondition(MultiApiVersion multiApiVersion, ApiVersion apiVersion) {
		this.multiApiVersion = multiApiVersion;
		this.apiVersion = apiVersion;
		if (isNull(multiApiVersion) && isNull(apiVersion)) {
			throw new IllegalStateException(format("Annotations %s and %s should not be empty at the same time.",
					MultiApiVersion.class.getSimpleName(), ApiVersion.class.getSimpleName()));
		}
		this.combineVersion = parseApiVersionValue(apiVersion);
	}

	protected MultiApiVersion getMultiApiVersion() {
		return multiApiVersion;
	}

	protected ApiVersion getApiVersion() {
		return apiVersion;
	}

	protected int getCombineVersion() {
		return combineVersion;
	}

	/**
	 * Parse API multi versions to int number.
	 * 
	 * @param v
	 * @return
	 */
	public static int parseApiVersionValue(ApiVersion v) {
		isTrue(v.major() >= 0, () -> format("Invalid @ApiVersion must major >= 0, actual: %s", v.major()));
		isTrue(v.minor() >= 0, () -> format("Invalid @ApiVersion must minor >= 0, actual: %s", v.minor()));
		isTrue(v.revision() >= 0, () -> format("Invalid @ApiVersion must revision >= 0, actual: %s", v.revision()));

		StringBuffer combine = new StringBuffer();
		combine.append(v.major());
		combine.append(v.minor());
		combine.append(v.revision());
		// Valid when the extended version number is greater than or equal to 0
		if (v.extension() >= 0) {
			combine.append(v.extension());
		}
		return Integer.parseInt(combine.toString());
	}

}
