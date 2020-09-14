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

import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.components.common.lang.Assert2.notNullOf;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.components.common.web.WebUtils2.checkRequestErrorStacktrace;
import static com.wl4g.components.common.web.WebUtils2.write;
import static com.wl4g.components.common.web.WebUtils2.writeJson;

import com.wl4g.components.common.jvm.JvmRuntimeKit;
import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.common.web.rest.RespBase;
import com.wl4g.components.core.config.ErrorControllerAutoConfiguration.ErrorHandlerProperties;
import com.wl4g.components.core.web.error.ErrorConfigurer.RenderingErrorHandler;

/**
 * Servlet smart global error controller.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0 2019年1月10日
 * @since
 */
@GlobalErrorController
@Order(Ordered.HIGHEST_PRECEDENCE)
// @ControllerAdvice
public class ServletSmartErrorController extends AbstractErrorController {

	protected final SmartLogger log = getLogger(getClass());

	/** {@link ErrorHandlerProperties} */
	protected final ErrorHandlerProperties config;

	/** {@link ErrorConfigurer} */
	protected final CompositeErrorConfigurer configurer;

	public ServletSmartErrorController(ErrorHandlerProperties config, ErrorAttributes errorAttributes,
			CompositeErrorConfigurer configurer) {
		super(errorAttributes);
		this.config = notNullOf(config, "config");
		this.configurer = notNullOf(configurer, "configurer");
	}

	/**
	 * Returns the path of the error page.
	 *
	 * @return the error path
	 */
	@Override
	public String getErrorPath() {
		return DEFAULT_PATH_ERROR;
	}

	/**
	 * DO any servlet request handler errors.
	 * 
	 * @param request
	 * @param response
	 * @param th
	 * @return
	 */
	@RequestMapping(DEFAULT_PATH_ERROR)
	@ExceptionHandler({ Throwable.class })
	public void doAnyHandleError(final HttpServletRequest request, final HttpServletResponse response, final Throwable th) {
		// Obtain errors attributes.
		Map<String, Object> model = getErrorAttributes(request, response, th);

		// handle errors
		configurer.handleGlobalErrors(request, model, th, new RenderingErrorHandler() {
			@Override
			public void renderingWithJson(Map<String, Object> model, RespBase<Object> resp) throws Exception {
				writeJson(response, resp.asJson());
			}

			@Override
			public void renderingWithView(Map<String, Object> model, int status, String renderString) throws Exception {
				write(response, status, TEXT_HTML_VALUE, renderString.getBytes(UTF_8));
			}

			@Override
			public void redirectError(Map<String, Object> model, String errorRedirectURI) throws Exception {
				response.sendRedirect(errorRedirectURI);
			}
		});
	}

	/**
	 * Whether error stack information is enabled
	 * 
	 * @param request
	 * @return
	 */
	private boolean isStackTrace(ServletRequest request) {
		if (log.isDebugEnabled() || JvmRuntimeKit.isJVMDebugging) {
			return true;
		}
		return checkRequestErrorStacktrace(request);
	}

	/**
	 * Extract error details model
	 * 
	 * @param request
	 * @return
	 */
	private Map<String, Object> getErrorAttributes(HttpServletRequest request, HttpServletResponse response, Throwable th) {
		boolean _stacktrace = isStackTrace(request);
		Map<String, Object> model = super.getErrorAttributes(request, _stacktrace);
		if (_stacktrace) {
			log.error("Origin Errors - {}", model);
		}

		// Replace the exception message that appears to be meaningful.
		model.put("message", configurer.getRootCause(model, th));
		return model;
	}

	final private static String DEFAULT_PATH_ERROR = "/error";

}