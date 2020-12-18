///*
// * Copyright (C) 2017 ~ 2025 the original author or authors.
// * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
// * All rights reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *      http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// * 
// * Reference to website: http://wl4g.com
// */
//package com.wl4g.component.rpc.feign.context;
//
//import static com.wl4g.component.common.collection.Collections2.safeEnumerationToList;
//import static com.wl4g.component.common.collection.Collections2.safeMap;
//import static com.wl4g.component.common.lang.Assert2.notNullOf;
//import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
//import static java.lang.String.valueOf;
//import static java.util.Locale.US;
//import static java.util.stream.Collectors.toSet;
//
//import java.util.Set;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.validation.constraints.NotNull;
//
//import com.wl4g.component.common.log.SmartLogger;
//
//import feign.RequestInterceptor;
//import feign.RequestTemplate;
//
///**
// * {@link AbstractFeignRequestInterceptor}
// * 
// * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
// * @version v1.0 2020-12-18
// * @sine v1.0
// * @see
// */
//public abstract class AbstractFeignRequestInterceptor implements RequestInterceptor {
//	protected final SmartLogger log = getLogger(getClass());
//
//	/**
//	 * Addition servlet request parameters to feign context.
//	 * 
//	 * @param template
//	 * @param request
//	 */
//	protected void addRequestParameters(RequestTemplate template, @NotNull HttpServletRequest request) {
//		// if (isNull(request)) {
//		// log.warn("Unable add request parameters to context, request is
//		// null");
//		// return;
//		// }
//		notNullOf(request, "request");
//
//		// Gets the request header already contained in the RequestTemplate
//		Set<String> existingHeaderNames = safeMap(template.headers()).keySet().stream().map(n -> valueOf(n).toLowerCase(US))
//				.collect(toSet());
//
//		// Addition request headers.
//		safeEnumerationToList(request.getHeaderNames()).forEach(name -> {
//			// If the request header is already included in the
//			// RequestTemplate header, skip
//			if (existingHeaderNames.contains(valueOf(name).toLowerCase(US))) {
//				log.debug("Skip add request header. {}", name);
//				return;
//			}
//			template.header(name, request.getHeader(name));
//		});
//
//		// Addition request parameters.
//		safeMap(request.getParameterMap()).forEach((name, values) -> {
//			template.query(name, values);
//		});
//	}
//
//}
