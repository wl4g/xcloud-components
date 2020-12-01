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

import static com.wl4g.components.common.collection.Collections2.safeArrayToList;
import static com.wl4g.components.common.lang.Assert2.isTrue;
import static com.wl4g.components.common.lang.Assert2.state;
import static java.util.Collections.synchronizedList;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import com.wl4g.components.common.collection.CollectionUtils2;
import com.wl4g.components.core.web.method.mapping.WebMvcHandlerMappingConfigurer.ServletHandlerMappingSupport;
import com.wl4g.components.core.web.versions.AmbiguousApiVersionMappingException;
import com.wl4g.components.core.web.versions.annotation.ApiVersion;
import com.wl4g.components.core.web.versions.annotation.ApiVersionGroup;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.result.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.condition.RequestCondition;
import org.springframework.core.Ordered;

import static org.springframework.core.annotation.AnnotatedElementUtils.findMergedAnnotation;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * API versions {@link RequestMapping} handler mapping. </br>
 * </br>
 * Notes: Only valid for all {@link RequestMapping} annotated beans (it will
 * override the spring default {@link RequestMappingHandlerMapping}(servlet)
 * instance).
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-26
 * @sine v1.0
 * @see <a href=
 *      "https://blog.csdn.net/sinat_29508581/article/details/89392831">Case2</a>
 * @see <a href=
 *      "http://www.kailaisii.com/archives/%E8%87%AA%E5%AE%9A%E4%B9%89RequestMappingHandlerMapping,%E5%86%8D%E4%B9%9F%E4%B8%8D%E7%94%A8%E5%86%99url%E4%BA%86~">Case1</a>
 * @see <a href=
 *      "https://blog.csdn.net/chuantian3080/article/details/100873706">Case3</a>
 * @see {@link org.springframework.web.servlet.DispatcherServlet#getHandler()}
 */
public class ServletVersionRequestHandlerMapping extends ServletHandlerMappingSupport {

	private String[] versionParams;
	private String[] groupParams;
	private Comparator<String> versionComparator;

	public ServletVersionRequestHandlerMapping() {
		setOrder(Ordered.HIGHEST_PRECEDENCE + 5);
	}

	public String[] getVersionParams() {
		return versionParams;
	}

	public void setVersionParams(String[] versionParams) {
		this.versionParams = versionParams;
	}

	public String[] getGroupParams() {
		return groupParams;
	}

	public void setGroupParams(String[] groupParams) {
		this.groupParams = groupParams;
	}

	public Comparator<String> getVersionComparator() {
		return versionComparator;
	}

	public void setVersionComparator(Comparator<String> versionComparator) {
		this.versionComparator = versionComparator;
	}

	@Override
	public void afterPropertiesSet() {
		// Clear useless mapping
		this.checkingMapping.clear();
	}

	// --- Request mapping conditions. ---

	@Override
	protected boolean supports(String beanName, Class<?> beanType) {
		return hasAnnotation(beanType, ApiVersion.class) || hasAnnotation(beanType, ApiVersionGroup.class);
	}

	@Override
	protected RequestCondition<ServletVersionCondition> getCustomTypeCondition(Class<?> handlerType) {
		return createCondition(handlerType);
	}

	@Override
	protected RequestCondition<ServletVersionCondition> getCustomMethodCondition(Method method) {
		return createCondition(method);
	}

	private final RequestCondition<ServletVersionCondition> createCondition(AnnotatedElement annotatedElement) {
		ApiVersionGroup apiVersionGroup = findAnnotation(annotatedElement, ApiVersionGroup.class);

		// Check version properties valid.
		checkVersionValid(annotatedElement, apiVersionGroup);

		return isNull(apiVersionGroup) ? null
				: new ServletVersionCondition(apiVersionGroup, getVersionComparator(), getVersionParams(), getGroupParams());
	}

	/**
	 * Check API version configuration properties whether valid.
	 * 
	 * @param element
	 * @param apiVersionGroup
	 * @param apiVersion
	 */
	protected void checkVersionValid(AnnotatedElement element, ApiVersionGroup apiVersionGroup) {
		// Check uniqueness. (version + requestPath + requestMethod)
		RequestMapping requestMapping = findMergedAnnotation(element, RequestMapping.class);
		state(!isNull(requestMapping), "Shouldn't be here");

		CheckMappingWrapper wrapper = new CheckMappingWrapper(requestMapping, apiVersionGroup);
		isTrue(!checkingMapping.contains(wrapper), AmbiguousApiVersionMappingException.class,
				"Ambiguous version API mapping, please ensure that the combination of version and requestPath and requestMethod is unique");

		this.checkingMapping.add(wrapper);
	}

	private final List<CheckMappingWrapper> checkingMapping = synchronizedList(new LinkedList<>());

	/**
	 * Used to check if the mapping is unique wrapper.
	 */
	class CheckMappingWrapper {

		private final List<RequestMethod> methods;
		private final List<String> path;
		private final List<String> version;

		public CheckMappingWrapper(RequestMapping requestMapping, ApiVersionGroup apiVersionGroup) {
			this.methods = safeArrayToList(requestMapping.method());
			List<String> paths = safeArrayToList(requestMapping.path());
			this.path = CollectionUtils2.isEmpty(paths) ? safeArrayToList(requestMapping.value()) : paths;
			this.version = safeArrayToList(apiVersionGroup.value()).stream().map(v -> v.value()).collect(toList());
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((methods == null) ? 0 : methods.hashCode());
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			result = prime * result + ((version == null) ? 0 : version.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CheckMappingWrapper other = (CheckMappingWrapper) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (methods == null) {
				if (other.methods != null)
					return false;
			} else if (!methods.equals(other.methods))
				return false;
			if (path == null) {
				if (other.path != null)
					return false;
			} else if (!path.equals(other.path))
				return false;
			if (version == null) {
				if (other.version != null)
					return false;
			} else if (!version.equals(other.version))
				return false;
			return true;
		}

		private ServletVersionRequestHandlerMapping getOuterType() {
			return ServletVersionRequestHandlerMapping.this;
		}

	}

}
