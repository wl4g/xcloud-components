/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.component.integration.feign.core.context.internal;

import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static java.util.Arrays.asList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;

import com.wl4g.component.integration.feign.core.context.RpcContextHolder;
import com.wl4g.component.integration.feign.core.context.internal.FeignContextCoprocessor.Invokers;

import feign.MethodMetadata;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * {@link ConsumerFeignContextInterceptor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-26
 * @sine v1.0
 * @see
 */
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class ConsumerFeignContextInterceptor implements RequestInterceptor {

	private final Map<Method, List<List<Annotation>>> knownMethodAnnotationCache = new ConcurrentHashMap<>(64);

	/**
	 * {@link feign.SynchronousMethodHandler#executeAndDecode(RequestTemplate, Options)}
	 */
	@Override
	public void apply(RequestTemplate template) {
		try {
			// Sets contentType.(if necessary)
			setContentTypeIfNecessary(template);

			// Before calling RPC, the current context attachment info should be
			// added to the request header.
			FeignRpcContextBinders.writeAttachmentsToFeignRequest(template);

			// Call coprocessor.
			Invokers.beforeConsumerExecution(template);
		} finally {
			// Before calling RPC, first cleanup server context, reference:
			// dubbo-2.7.4.1↓:ConsumerContextFilter.java
			RpcContextHolder.removeServerContext();
		}
	}

	private void setContentTypeIfNecessary(RequestTemplate template) {
		Annotation requestBody = getMethodParameterAnnotation(template.methodMetadata(), RequestBody.class);
		if (nonNull(requestBody)) { // POST body request?
			// Default sets application/json
			template.header("Content-Type", MediaType.APPLICATION_JSON_VALUE);
		}
	}

	private Annotation getMethodParameterAnnotation(MethodMetadata metadata, Class<? extends Annotation> findAnnotation) {
		for (List<Annotation> annos : loadMethodParameterAnnotations(metadata)) {
			for (Annotation anno : annos) {
				if (findAnnotation == anno.getClass() || findAnnotation.isAssignableFrom(anno.getClass())) {
					return anno;
				}
			}
		}
		return null;
	}

	private List<List<Annotation>> loadMethodParameterAnnotations(MethodMetadata metadata) {
		List<List<Annotation>> annotations = knownMethodAnnotationCache.get(metadata.method());
		if (isNull(annotations)) {
			synchronized (this) {
				annotations = knownMethodAnnotationCache.get(metadata.method());
				if (isNull(annotations)) {
					annotations = safeArrayToList(metadata.method().getParameterAnnotations()).stream()
							.map(annos -> asList(annos)).collect(toList());
					knownMethodAnnotationCache.put(metadata.method(), annotations);
				}
			}
		}
		return annotations;
	}

}