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

import static com.wl4g.components.common.lang.Assert2.notNullOf;
import static com.wl4g.components.common.lang.StringUtils2.isTrue;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.components.common.web.WebUtils2.PARAM_STACKTRACE;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static java.util.Locale.US;

import java.net.URI;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.AbstractErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_HTML;
import static org.springframework.http.HttpStatus.OK;

import com.wl4g.components.common.jvm.JvmRuntimeKit;
import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.common.web.CookieUtils;
import com.wl4g.components.common.web.rest.RespBase;
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

	protected final SmartLogger log = getLogger(getClass());

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
		Map<String, Object> model = getErrorAttributes(request, true);

		// TODO
		// redirect
		ServerResponse.status(-111).contentType(TEXT_HTML).location(URI.create("")).build();

		// rendering
		ServerResponse.status(-111).contentType(TEXT_HTML).body(BodyInserters.fromValue(model));

		// json
		return ServerResponse.ok().contentType(APPLICATION_JSON).body(BodyInserters.fromValue(RespBase.create()));
	}

	/**
	 * {@link ReactiveErrorAttributes}
	 */
	public static class ReactiveErrorAttributes extends DefaultErrorAttributes {

		protected final SmartLogger log = getLogger(getClass());

		/** {@link ErrorConfigurer} */
		protected final CompositeErrorConfigurer configurer;

		public ReactiveErrorAttributes(CompositeErrorConfigurer configurer) {
			super(true);
			this.configurer = notNullOf(configurer, "configurer");
		}

		@Override
		public Map<String, Object> getErrorAttributes(ServerRequest request, boolean includeStackTrace) {
			boolean _stacktrace = isStackTrace(request);
			Map<String, Object> model = super.getErrorAttributes(request, _stacktrace);
			if (_stacktrace) {
				log.error("Origin Errors - {}", model);
			}

			// Replace the exception message that appears to be meaningful.
			model.put("message", configurer.getRootCause(model, getError(request)));
			return model;
		}

		/**
		 * Whether error stack information is enabled
		 * 
		 * @param request
		 * @return
		 */
		private boolean isStackTrace(ServerRequest request) {
			if (log.isDebugEnabled() || JvmRuntimeKit.isJVMDebugging) {
				return true;
			}

			String _stacktraceVal = request.queryParam(PARAM_STACKTRACE).orElse(null);
			if (isBlank(_stacktraceVal) && request instanceof HttpServletRequest) {
				_stacktraceVal = CookieUtils.getCookie((HttpServletRequest) request, PARAM_STACKTRACE);
			}
			if (isBlank(_stacktraceVal)) {
				return false;
			}
			return isTrue(_stacktraceVal.toLowerCase(US), false);
		}

	}

}