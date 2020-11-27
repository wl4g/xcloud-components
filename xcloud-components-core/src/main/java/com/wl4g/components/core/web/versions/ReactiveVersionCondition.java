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

import org.springframework.web.reactive.result.condition.RequestCondition;
import org.springframework.web.server.ServerWebExchange;

/**
 * Reactive API versions number rules condition.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see
 */
public class ReactiveVersionCondition extends AbstractVersionRequestCondition
		implements RequestCondition<ReactiveVersionCondition> {

	private final ApiVersion apiVersion;
	private final int combineVersion;

	public ReactiveVersionCondition(ApiVersion apiVersion) {
		this.apiVersion = notNullOf(apiVersion, "apiVersion");
		this.combineVersion = parseApiVersion(apiVersion);
	}

	@Override
	public ReactiveVersionCondition combine(ReactiveVersionCondition other) {
		// 采用最后定义优先原则，则方法上的定义覆盖类上面的定义
		return new ReactiveVersionCondition(other.getApiVersion());
	}

	@Override
	public ReactiveVersionCondition getMatchingCondition(ServerWebExchange request) {
		String ver = request.getRequest().getHeaders().getFirst("Api-Version");
		// 因为请求头里面传来的是小数，所以需要乘以10
		int version = (int) (Double.valueOf(ver) * 10);
		// 如果请求的版本号大于等于配置版本号， 则满足
		if (version >= this.combineVersion) {
			return this;
		}
		return null;
	}

	@Override
	public int compareTo(ReactiveVersionCondition other, ServerWebExchange request) {
		// Matchs the latest version number first.
		return other.getCombineVersion() - this.combineVersion;
	}

	public ApiVersion getApiVersion() {
		return apiVersion;
	}

	public int getCombineVersion() {
		return combineVersion;
	}

}
