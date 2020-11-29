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

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.mvc.condition.RequestCondition;

import com.wl4g.components.core.web.versions.annotation.ApiVersion;
import com.wl4g.components.core.web.versions.annotation.MultiApiVersion;

/**
 * Servlet mvc API versions number rules condition.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see
 */
public class ServletVersionCondition extends AbstractVersionRequestCondition
		implements RequestCondition<ServletVersionCondition> {

	public ServletVersionCondition(MultiApiVersion multiApiVersion, ApiVersion apiVersion) {
		super(multiApiVersion, apiVersion);
	}

	@Override
	public ServletVersionCondition combine(ServletVersionCondition other) {
		// Use the nearest definition priority principle, that is, the
		// definition on the method covers the definition above the type.
		return new ServletVersionCondition(other.getMultiApiVersion(), other.getApiVersion());
	}

	@Override
	public ServletVersionCondition getMatchingCondition(HttpServletRequest request) {
		//TODO
		
//		String ver = request.getHeader("Api-Version");
//		// 因为请求头里面传来的是小数，所以需要乘以10
//		int version = (int) (Double.valueOf(ver) * 10);
//		// 如果请求的版本号大于等于配置版本号， 则满足
//		if (version >= this.combineVersion) {
//			return this;
//		}
		return null;
	}

	@Override
	public int compareTo(ServletVersionCondition other, HttpServletRequest request) {
		//TODO
		
		// Matchs the latest version number first.
		return other.getCombineVersion() - this.combineVersion;
	}

}
