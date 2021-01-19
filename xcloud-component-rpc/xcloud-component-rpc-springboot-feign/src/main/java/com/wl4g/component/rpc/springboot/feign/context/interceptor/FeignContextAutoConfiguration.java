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
package com.wl4g.component.rpc.springboot.feign.context.interceptor;

import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.component.common.lang.ClassUtils2.resolveClassNameNullable;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.core.utils.web.WebUtils3.currentServletRequest;
import static com.wl4g.component.core.utils.web.WebUtils3.currentServletResponse;
import static java.util.Objects.nonNull;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.core.framework.proxy.SmartProxyProcessor;
import com.wl4g.component.rpc.springboot.feign.annotation.SpringBootFeignClient;
import com.wl4g.component.rpc.springboot.feign.context.FeignContextBinders;
import com.wl4g.component.rpc.springboot.feign.context.RpcContextHolder;

/***
 * {@link FeignContextAutoConfiguration}
 *
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-07
 * @sine v1.0
 * @see
 */
@Configuration // auto-configuration(client|server)
@ConditionalOnWebApplication(type = Type.SERVLET)
public class FeignContextAutoConfiguration {
	protected final SmartLogger log = getLogger(getClass());

	@Bean
	public FeignContextRequestInterceptor feignContextRequestInterceptor() {
		return new FeignContextRequestInterceptor();
	}

	@Bean
	public HeadersRequestInterceptor headersRequestInterceptor() {
		return new HeadersRequestInterceptor();
	}

	@Bean
	public FeignContextProxyProcessor feignContextProxyProcessor() {
		return new FeignContextProxyProcessor();
	}

	/**
	 * Notes: {@link RequestBodyAdvice} should not be used here because not all
	 * request parameters are in the request.body , which may not perform
	 * binding to {@link RpcContextHolder}
	 */
	public static final class FeignContextProxyProcessor implements SmartProxyProcessor {

		@Override
		public int getOrder() {
			return ORDER;
		}

		@Override
		public boolean supportTypeProxy(Object target, Class<?> actualOriginalTargetClass) {
			return checkSupportTypeProxy(target, actualOriginalTargetClass);
		}

		@Override
		public boolean supportMethodProxy(Object target, Method method, Class<?> actualOriginalTargetClass, Object... args) {
			return checkSupportMethodProxy(target, method, actualOriginalTargetClass, args);
		}

		@Override
		public Object[] preHandle(@NotNull Object target, @NotNull Method method, Object[] parameters) {
			HttpServletRequest request = currentServletRequest();
			// When receiving RPC requests, the attachment info should be
			// extracted and bound to the local context.
			FeignContextBinders.bindAttachmentsFromRequest(request);
			return parameters;
		}

		@Override
		public Object postHandle(@NotNull Object target, @NotNull Method method, Object[] parameters, Object result,
				@NotNull Throwable ex) {
			try {
				HttpServletResponse response = currentServletResponse();
				// When responding to RPC, the attachment information returned
				// should be added.
				FeignContextBinders.writeAttachemntsToResponse(response);
			} finally {
				// After responding to RPC, should cleanup the context
				// attachment info.
				RpcContextHolder.get().clearAttachments();
			}
			return result;
		}

		public static boolean checkSupportTypeProxy(Object target, Class<?> actualOriginalTargetClass) {
			if (hasAnnotation(actualOriginalTargetClass, RequestMapping.class)
					&& hasAnnotation(actualOriginalTargetClass, ResponseBody.class)) {
				return true;
			} else {
				for (Class<?> interfaceClass : safeArrayToList(actualOriginalTargetClass.getInterfaces())) {
					if (hasAnnotation(interfaceClass, SpringBootFeignClient.class)
							|| (nonNull(FEIGN_CLIENT_CLASS) && hasAnnotation(interfaceClass, FEIGN_CLIENT_CLASS))) {
						return true;
					}
				}
			}
			return false;
		}

		public static boolean checkSupportMethodProxy(Object target, Method method, Class<?> actualOriginalTargetClass,
				Object... args) {
			return hasAnnotation(method.getDeclaringClass(), ResponseBody.class) || hasAnnotation(method, ResponseBody.class);
		}

		public static final Class<? extends Annotation> FEIGN_CLIENT_CLASS = resolveClassNameNullable(
				"org.springframework.cloud.openfeign.FeignClient");

		public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 10;
	}

}
