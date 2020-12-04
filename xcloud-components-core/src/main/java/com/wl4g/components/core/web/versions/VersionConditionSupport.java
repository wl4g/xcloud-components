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
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
import javax.servlet.http.HttpServletRequest;

import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.core.web.versions.annotation.ApiVersionMapping;
import com.wl4g.components.core.web.versions.annotation.ApiVersionMappingWrapper;
import com.wl4g.components.core.web.versions.annotation.EnableApiVersionMapping;
import com.wl4g.components.core.web.versions.annotation.EnableApiVersionMappingWrapper;

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

	/**
	 * API version mapping wrapper, attributes from {@link ApiVersionMapping}
	 * and {@link EnableApiVersionMapping}
	 */
	private final ApiVersionMappingWrapper versionMapping;

	/**
	 * {@link org.springframework.web.servlet.mvc.method.RequestMappingInfo#getMatchingCondition(HttpServletRequest)}
	 */
	private final List<String> matchedCandidateVersions;

	public VersionConditionSupport(ApiVersionMappingWrapper versionMapping) {
		this(versionMapping, null);
	}

	public VersionConditionSupport(ApiVersionMappingWrapper versionMapping, List<String> matchedCandidateVersions) {
		this.versionMapping = notNullOf(versionMapping, "versionMapping");
		this.matchedCandidateVersions = matchedCandidateVersions;
	}

	/**
	 * Gets request parameter value.
	 * 
	 * @param request
	 * @param names
	 * @return
	 */
	protected String findRequestParameter(HttpServletRequest request, String[] names) {
		for (String name : names) {
			String value = request.getParameter(name);
			value = isBlank(value) ? request.getHeader(name) : value;
			if (!isBlank(value)) {
				return value;
			}
		}
		return null;
	}

	public ApiVersionMappingWrapper getVersionMapping() {
		return versionMapping;
	}

	public List<String> getMatchedCandidateVersions() {
		return matchedCandidateVersions;
	}

	protected EnableApiVersionMappingWrapper getVersionConfig() {
		return versionMapping.getVersionConfig();
	}

}
