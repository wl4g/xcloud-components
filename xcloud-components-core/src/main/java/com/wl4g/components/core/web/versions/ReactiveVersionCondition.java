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

import org.springframework.web.reactive.result.condition.RequestCondition;
import org.springframework.web.server.ServerWebExchange;

import com.wl4g.components.core.web.versions.annotation.ApiVersion;
import com.wl4g.components.core.web.versions.annotation.MultiApiVersion;

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

	public ReactiveVersionCondition(MultiApiVersion multiApiVersion, ApiVersion apiVersion) {
		super(multiApiVersion, apiVersion);
	}

	@Override
	public ReactiveVersionCondition combine(ReactiveVersionCondition other) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReactiveVersionCondition getMatchingCondition(ServerWebExchange exchange) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(ReactiveVersionCondition other, ServerWebExchange exchange) {
		// TODO Auto-generated method stub
		return 0;
	}

}