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

import com.wl4g.components.common.lang.StringUtils2;
import com.wl4g.components.core.web.versions.VersionConditionSupport;
import com.wl4g.components.core.web.versions.annotation.ApiVersion;
import com.wl4g.components.core.web.versions.annotation.ApiVersionMapping;

/**
 * Servlet mvc API versions number rules condition.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see https://blog.csdn.net/sinat_29508581/article/details/89392831
 */
public class ServletVersionCondition extends VersionConditionSupport implements RequestCondition<ServletVersionCondition> {

	public ServletVersionCondition(ApiVersionMapping apiVersionGroup, Comparator<String> versionComparator,
			String[] versionParams, String[] groupParams) {
		super(apiVersionGroup, versionComparator, versionParams, groupParams);
	}

	@Override
	public ServletVersionCondition combine(ServletVersionCondition other) {
		// Use the nearest definition priority principle, that is, the
		// definition on the method covers the definition above the type.
		return new ServletVersionCondition(other.getApiVersionMapping(), getVersionComparator(), other.getVersionParams(),
				other.getGroupParams());
	}

	/**
	 * When entering this method, it indicates that the URL has been matched.
	 * What needs to be handled here is to match other request conditions (such
	 * as request header, version, etc.). </br>
	 * refer:
	 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#lookupHandlerMethod()}
	 * and
	 * {@link org.springframework.web.servlet.handler.AbstractHandlerMethodMapping#addMatchingMappings()}
	 * 
	 * @see <a href=
	 *      "https://my.oschina.net/zhangxufeng/blog/2177464">https://my.oschina.net/zhangxufeng/blog/2177464</a>
	 */
	@Override
	public ServletVersionCondition getMatchingCondition(HttpServletRequest request) {
		// Only api with backward compatible versions are matched.

		String requestVer = getRequestParameter(request, getVersionParams());
		String requestVerGroup = getRequestParameter(request, getGroupParams());
		log.debug("Comparing rqeuest version: {}, group: {}", requestVer, requestVerGroup);

		for (ApiVersion ver : getApiVersionMapping().value()) {
			for (String group : ver.clientGroups()) {
				if (getMatcher().compare(group, requestVerGroup) > 0) { // matchs-group
					if (getVersionComparator().compare(ver.value(), requestVer) > 1) {
						return this;
					}
				}
			}
		}

		return this;
	}

	@Override
	public int compareTo(ServletVersionCondition other, HttpServletRequest request) {
		// Matchs the latest version number first.
		String versionReq = getRequestParameter(request, getVersionParams());
		String versionGroupReq = getRequestParameter(request, getGroupParams());
		log.debug("Comparing rqeuest version: {}, group: {}", versionReq, versionGroupReq);

		// TODO
		for (ApiVersion ver : getApiVersionMapping().value()) {
			if (eqIgnCase(ver, versionReq)) {
				return getVersionComparator().compare(versionReq, "1");
			}
		}

		return getVersionComparator().compare(versionReq, "1");
	}

	private Comparator<String> getMatcher() {
		return getApiVersionMapping().sensitive() ? sensitiveMatcher : unsensitiveMatcher;
	}

	private static final Comparator<String> sensitiveMatcher = (o1, o2) -> StringUtils2.equals(o1, o2) ? 1 : 0;
	private static final Comparator<String> unsensitiveMatcher = (o1, o2) -> eqIgnCase(o1, o2) ? 1 : 0;

}
