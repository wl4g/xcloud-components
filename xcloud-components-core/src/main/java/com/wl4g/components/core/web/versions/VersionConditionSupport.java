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

import static com.wl4g.components.common.collection.Collections2.safeArrayToList;
import static com.wl4g.components.common.lang.Assert2.notNullOf;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Comparator;

import javax.servlet.http.HttpServletRequest;

import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.core.web.versions.annotation.ApiVersionMapping;

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

	private final ApiVersionMapping apiVersionMapping;
	private final Comparator<String> versionComparator;

	// Request obtain versions info parameters.
	private final String[] versionParams;
	private final String[] groupParams;

	public VersionConditionSupport(ApiVersionMapping apiVersionGroup, Comparator<String> versionComparator, String[] versionParams,
			String[] groupParams) {
		this.apiVersionMapping = notNullOf(apiVersionGroup, "apiVersionGroup");
		this.versionComparator = notNullOf(versionComparator, "versionComparator");
		this.versionParams = safeArrayToList(versionParams).toArray(new String[0]);
		this.groupParams = safeArrayToList(groupParams).toArray(new String[0]);
	}

	/**
	 * Gets request parameter value.
	 * 
	 * @param request
	 * @param names
	 * @return
	 */
	protected String getRequestParameter(HttpServletRequest request, String[] names) {
		for (String name : names) {
			String value = request.getParameter(name);
			value = isBlank(value) ? request.getHeader(name) : value;
			if (!isBlank(value)) {
				return value;
			}
		}
		return null;
	}

	public ApiVersionMapping getApiVersionMapping() {
		return apiVersionMapping;
	}

	public Comparator<String> getVersionComparator() {
		return versionComparator;
	}

	public String[] getVersionParams() {
		return versionParams;
	}

	public String[] getGroupParams() {
		return groupParams;
	}

}
