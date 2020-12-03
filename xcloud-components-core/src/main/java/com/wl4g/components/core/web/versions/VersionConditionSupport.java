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
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.env.Environment;

import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.core.web.versions.annotation.ApiVersion;
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

	private final Environment environment;

	/**
	 * Whether to be case sensitive when matching request parameters.
	 */
	private final boolean sensitive;
	private final ApiVersionMapping apiVersionMapping;
	private final List<ApiVersionMappingWrapper> apiVersionMappingWrappers;

	// Request api versions comparator.
	private final SimpleVersionComparator versionComparator;

	// Extract version info parameters.
	private final String[] versionParams;
	private final String[] groupParams;

	/**
	 * {@link org.springframework.web.servlet.mvc.method.RequestMappingInfo#getMatchingCondition(HttpServletRequest)}
	 */
	private final List<String> matchedCandidateVersions;

	public VersionConditionSupport(Environment environment, ApiVersionMapping versionMapping,
			Comparator<String> versionComparator, String[] versionParams, String[] groupParams) {
		this(environment, versionMapping, versionComparator, versionParams, groupParams, null);
	}

	public VersionConditionSupport(Environment environment, ApiVersionMapping versionMapping,
			Comparator<String> versionComparator, String[] versionParams, String[] groupParams,
			List<String> matchedCandidateVersions) {
		this.environment = notNullOf(environment, "environment");
		this.apiVersionMapping = notNullOf(versionMapping, "versionMapping");
		this.apiVersionMappingWrappers = resolveMappingProperties(environment, versionMapping);
		this.sensitive = versionMapping.sensitive();
		this.versionComparator = notNullOf(versionComparator, "versionComparator");
		this.versionParams = safeArrayToList(versionParams).toArray(new String[0]);
		this.groupParams = safeArrayToList(groupParams).toArray(new String[0]);
		this.matchedCandidateVersions = matchedCandidateVersions;
	}

	private List<ApiVersionMappingWrapper> resolveMappingProperties(Environment environment, ApiVersionMapping versionMapping) {
		List<ApiVersionMappingWrapper> wrappers = new ArrayList<>();
		for (ApiVersion ver : safeArrayToList(versionMapping.value())) {
			if ((nonNull(ver) && !isBlank(ver.value()))) {
				String[] groups = safeArrayToList(ver.groups()).stream().filter(g -> !isBlank(g))
						.map(g -> environment.resolveRequiredPlaceholders(g)).toArray(String[]::new);
				wrappers.add(new ApiVersionMappingWrapper(groups, environment.resolveRequiredPlaceholders(ver.value())));
			}
		}

		return wrappers;
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

	public Environment getEnvironment() {
		return environment;
	}

	public boolean getSensitive() {
		return sensitive;
	}

	public ApiVersionMapping getApiVersionMapping() {
		return apiVersionMapping;
	}

	public List<ApiVersionMappingWrapper> getApiVersionMappingWrappers() {
		return apiVersionMappingWrappers;
	}

	public SimpleVersionComparator getVersionComparator() {
		return versionComparator;
	}

	public String[] getVersionParams() {
		return versionParams;
	}

	public String[] getGroupParams() {
		return groupParams;
	}

	public List<String> getMatchedCandidateVersions() {
		return matchedCandidateVersions;
	}

	/**
	 * {@link ApiVersionMapping} annotation metadata wrapper.
	 */
	public static class ApiVersionMappingWrapper {

		/**
		 * Resolved placeholders value of {@link ApiVersion#groups()}
		 */
		private final String[] groups;

		/**
		 * Resolved placeholders value of {@link ApiVersion#value()}
		 */
		private final String value;

		public ApiVersionMappingWrapper(String[] groups, String value) {
			this.groups = groups;
			this.value = value;
		}

		public String[] getGroups() {
			return groups;
		}

		public String getValue() {
			return value;
		}

	}

}
