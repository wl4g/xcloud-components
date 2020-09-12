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
package com.wl4g.components.core.web.error;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.wl4g.components.core.config.ErrorControllerAutoConfiguration.ErrorHandlerProperties;

import reactor.core.publisher.Mono;

/**
 * Reactive smart global web error handler.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-12
 * @sine v1.0.0
 * @see https://blog.csdn.net/keets1992/article/details/85077874
 */
// @ControllerAdvice
public class ReactiveSmartErrorHandler extends AbstractErrorWebExceptionHandler implements InitializingBean {

	/** {@link ErrorHandlerProperties} */
	@Autowired
	protected ErrorHandlerProperties config;

	public ReactiveSmartErrorHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
			ApplicationContext actx) {
		super(errorAttributes, resourceProperties, actx);
	}

	@Override
	protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
		return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
	}

	private Mono<ServerResponse> renderErrorResponse(final ServerRequest request) {
		Map<String, Object> errorPropertiesMap = getErrorAttributes(request, true);
		return ServerResponse.status(HttpStatus.OK).contentType(MediaType.APPLICATION_JSON)
				.body(BodyInserters.fromValue(errorPropertiesMap));
	}

	/**
	 * {@link ReactiveErrorAttributes}
	 */
	public static class ReactiveErrorAttributes extends DefaultErrorAttributes {

		public ReactiveErrorAttributes() {
			super(false);
		}

		@Override
		public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
			return assembleError(request);
		}

		private Map<String, Object> assembleError(ServerRequest request) {
			Map<String, Object> errorAttributes = new LinkedHashMap<>();
			Throwable error = getError(request);
			errorAttributes.put("code", HttpStatus.INTERNAL_SERVER_ERROR);
			errorAttributes.put("data", error.getMessage());

			return errorAttributes;
		}

	}

}