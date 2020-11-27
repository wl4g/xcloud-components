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
package com.wl4g.components.rpc.springcloud.feign.web;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import static com.wl4g.components.rpc.springcloud.util.FeignDubboUtils.isFeignProxyBean;
import static java.util.Objects.nonNull;

/**
 * {@link FeignServletRequestHandlerMapping}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-26
 * @sine v1.0
 * @see {@link org.springframework.web.servlet.DispatcherServlet#getHandler()}
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class FeignServletRequestHandlerMapping extends RequestMappingHandlerMapping {

	@Override
	protected void processCandidateBean(String beanName) {
		Class<?> beanType = null;
		try {
			beanType = obtainApplicationContext().getType(beanName);
		} catch (Throwable ex) {
			// An unresolvable bean type, probably from a lazy bean -
			// let's ignore it.
			logger.trace("Could not resolve type for bean '" + beanName + "'", ex);
		}

		if (nonNull(beanType)) {
			// In order to solve the following problems, spring will
			// scan the @RequestMapping annotation class by default and
			// inject it into MapperRegistry, which will cause conflicts
			// with the dynamically generated '$FeignProxyController'
			// class.
			if (hasAnnotation(beanType, FeignClient.class) && isFeignProxyBean(beanName)) {
				super.detectHandlerMethods(beanName);
			}
		}

	}

}
