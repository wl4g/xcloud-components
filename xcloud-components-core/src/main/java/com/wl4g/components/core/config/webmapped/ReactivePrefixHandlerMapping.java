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
package com.wl4g.components.core.config.webmapped;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.reactive.result.condition.PatternsRequestCondition;
import org.springframework.web.reactive.result.method.RequestMappingInfo;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;

/**
 * {@link ReactivePrefixHandlerMapping}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-12
 * @sine v1.0.0
 * @see
 */
public class ReactivePrefixHandlerMapping extends RequestMappingHandlerMapping implements PrefixHandlerMapping {

	private String prefix = "";
	private final Object handlers[];

	public ReactivePrefixHandlerMapping(Object... handlers) {
		this.handlers = handlers.clone();
		setOrder(-50);
	}

	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		for (Object handler : handlers) {
			detectHandlerMethods(handler);
		}
	}

	@Override
	protected boolean isHandler(Class<?> beanType) {
		return false;
	}

	@Override
	protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
		if (mapping == null) {
			return;
		}
		super.registerHandlerMethod(handler, method, withPrefix(mapping));
	}

	private RequestMappingInfo withPrefix(RequestMappingInfo mapping) {
		List<String> newPatterns = getPatterns(mapping);

		PatternsRequestCondition patterns = new PatternsRequestCondition(newPatterns.toArray(new String[newPatterns.size()]));
		return new RequestMappingInfo(patterns, mapping.getMethodsCondition(), mapping.getParamsCondition(),
				mapping.getHeadersCondition(), mapping.getConsumesCondition(), mapping.getProducesCondition(),
				mapping.getCustomCondition());
	}

	private List<String> getPatterns(RequestMappingInfo mapping) {
		List<String> newPatterns = new ArrayList<String>(mapping.getPatternsCondition().getPatterns().size());
		for (String pattern : mapping.getPatternsCondition().getPatterns()) {
			newPatterns.add(prefix + pattern);
		}
		return newPatterns;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getPrefix() {
		return prefix;
	}

}