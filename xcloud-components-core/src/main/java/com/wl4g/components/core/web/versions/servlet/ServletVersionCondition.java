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
package com.wl4g.components.core.web.versions.servlet;

import static com.wl4g.components.common.lang.StringUtils2.eqIgnCase;

import java.util.Comparator;
import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.mvc.condition.RequestCondition;

import com.wl4g.components.core.web.versions.VersionConditionSupport;
import com.wl4g.components.core.web.versions.annotation.ApiVersion;
import com.wl4g.components.core.web.versions.annotation.ApiVersionGroup;

/**
 * Servlet mvc API versions number rules condition.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see https://blog.csdn.net/sinat_29508581/article/details/89392831
 */
public class ServletVersionCondition extends VersionConditionSupport implements RequestCondition<ServletVersionCondition> {

	public ServletVersionCondition(ApiVersionGroup apiVersionGroup, Comparator<String> versionComparator, String[] versionParams,
			String[] groupParams) {
		super(apiVersionGroup, versionComparator, versionParams, groupParams);
	}

	@Override
	public ServletVersionCondition combine(ServletVersionCondition other) {
		// Use the nearest definition priority principle, that is, the
		// definition on the method covers the definition above the type.
		return new ServletVersionCondition(other.getApiVersionGroup(), getVersionComparator(), other.getVersionParams(),
				other.getGroupParams());
	}

	@Override
	public ServletVersionCondition getMatchingCondition(HttpServletRequest request) {
		return this;
	}

	@Override
	public int compareTo(ServletVersionCondition other, HttpServletRequest request) {
		// Matchs the latest version number first.
		String versionReq = getRequestParameter(request, getVersionParams());
		String versionGroupReq = getRequestParameter(request, getGroupParams());
		log.debug("Comparing rqeuest version: {}, group: {}", versionReq, versionGroupReq);

		// TODO
		for (ApiVersion ver : getApiVersionGroup().value()) {
			if (eqIgnCase(ver, versionReq)) {
				return getVersionComparator().compare(versionReq, "1");
			}
		}

		return getVersionComparator().compare(versionReq, "1");
	}

}
