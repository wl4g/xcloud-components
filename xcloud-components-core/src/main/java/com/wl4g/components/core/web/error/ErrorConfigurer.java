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

import static com.google.common.base.Charsets.UTF_8;
import static com.wl4g.components.common.collection.Collections2.safeMap;
import static com.wl4g.components.common.lang.Assert2.hasText;
import static com.wl4g.components.common.lang.Assert2.notNull;
import static com.wl4g.components.common.lang.Assert2.notNullOf;
import static com.wl4g.components.common.lang.Exceptions.getStackTraceAsString;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.components.common.web.WebUtils2.ResponseType.isRespJSON;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;
import static org.springframework.ui.freemarker.FreeMarkerTemplateUtils.processTemplateIntoString;
import static java.util.Objects.nonNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;

import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.common.view.Freemarkers;
import com.wl4g.components.common.web.rest.RespBase;
import com.wl4g.components.common.web.rest.RespBase.RetCode;
import com.wl4g.components.core.config.ErrorControllerAutoConfiguration.ErrorHandlerProperties;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import reactor.core.publisher.Mono;

/**
 * Error configuration adapter.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2019年11月1日
 * @since
 */
public abstract class ErrorConfigurer implements InitializingBean {

	protected final SmartLogger log = getLogger(getClass());

	/** Errors configuration properties. */
	protected final ErrorHandlerProperties config;

	/** Errors {@link Template} cache. */
	protected final Map<Integer, Template> errorTplMappingCache;

	public ErrorConfigurer(ErrorHandlerProperties config) {
		this.config = notNullOf(config, "config");
		this.errorTplMappingCache = new ConcurrentHashMap<>(4);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Initializing global smart error configurer ...");

		Configuration fmc = Freemarkers.create(config.getBasePath()).build();
		safeMap(config.getRenderingMapping()).entrySet().stream().forEach(p -> {
			try {
				if (!isErrorRedirectURI(p.getValue())) { // e.g 404.tpl.html
					Template tpl = fmc.getTemplate(p.getValue(), UTF_8.name());
					notNull(tpl, "Default (%s) error template must not be null", p.getKey());
					errorTplMappingCache.put(p.getKey(), tpl);
				}
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		});
	}

	/**
	 * Obtain exception {@link HttpStatus}
	 * 
	 * @param model
	 * @param th
	 * @return
	 */
	public abstract Integer getStatus(Map<String, Object> model, Throwable th);

	/**
	 * Obtain exception as string.
	 * 
	 * @param model
	 * @param th
	 * @return
	 */
	public abstract String getRootCause(Map<String, Object> model, Throwable th);

	/**
	 * Do automatic handling errors.
	 * 
	 * @param queryFetch
	 *            request query parameter fetcher.
	 * @param headers
	 *            request headers.
	 * @param model
	 * @param th
	 * @return handle errors result(if necessary). for example: {@link Mono}
	 */
	public Object autoHandleGlobalErrors(@NotNull Function<String, String> queryFetch, @NotEmpty Map<String, String> headers,
			@NotNull Map<String, Object> model, @NotNull Throwable th, @NotNull RenderingErrorHandler errorHandler) {

		try {
			// Obtain custom extension response status.
			int status = getStatus(model, th);
			String errmsg = getRootCause(model, th);

			// Gets redirectUrl or rendering template.
			Object uriOrTpl = getRedirectUriOrRenderErrorView(status);

			// If and only if the client is a browser and not an XHR request
			// returns to the page, otherwise it returns to JSON.
			if (isRespJSON(queryFetch, headers, null)) {
				RespBase<Object> resp = new RespBase<>(RetCode.newCode(status, errmsg));
				if (!(uriOrTpl instanceof Template)) {
					resp.forMap().put(DEFAULT_REDIRECT_KEY, uriOrTpl);
				}
				log.error("Resp errjson => {}", resp.asJson());
				return errorHandler.renderingWithJson(model, resp);
			}
			// Rendering errors view
			else {
				if (uriOrTpl instanceof Template) {
					log.error("Redirect errview => http({})", status);
					// Merge configuration to model.
					model.putAll(config.asMap());
					// Rendering
					String renderString = processTemplateIntoString((Template) uriOrTpl, model);
					return errorHandler.renderingWithView(model, status, renderString);
				} else {
					log.error("Redirect errview => {}", uriOrTpl);
					return errorHandler.redirectError(model, (String) uriOrTpl);
				}
			}
		} catch (Throwable th0) {
			log.error("Unable to handle global errors, origin cause: \n{} at causes:\n{}", getStackTraceAsString(th),
					getStackTraceAsString(th0));
		}

		return null;
	}

	/**
	 * Extract meaningful valid errors messages.
	 * 
	 * @param model
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected String extractValidErrorsMessage(@NotNull Map<String, Object> model) {
		notNull(model, "Shouldn't be here");

		StringBuffer errmsg = new StringBuffer();
		Object message = model.get("message");
		if (message != null) {
			errmsg.append(message);
		}

		Object errors = model.get("errors"); // @NotNull?
		if (errors != null) {
			errmsg.setLength(0); // Print only errors information
			if (errors instanceof Collection) {
				// Used to remove duplication
				List<String> fieldErrs = new ArrayList<>(8);

				Collection<Object> _errors = (Collection) errors;
				Iterator<Object> it = _errors.iterator();
				while (it.hasNext()) {
					Object err = it.next();
					if (err instanceof FieldError) {
						FieldError ferr = (FieldError) err;
						/*
						 * Remove duplicate field validation errors,
						 * e.g. @NotNull and @NotEmpty
						 */
						String fieldErr = ferr.getField();
						if (!fieldErrs.contains(fieldErr)) {
							errmsg.append("'");
							errmsg.append(fieldErr);
							errmsg.append("' ");
							errmsg.append(ferr.getDefaultMessage());
							errmsg.append(", ");
						}
						fieldErrs.add(fieldErr);
					} else {
						errmsg.append(err.toString());
						errmsg.append(", ");
					}
				}
			} else {
				errmsg.append(errors.toString());
			}
		}

		return errmsg.toString();
	}

	/**
	 * Gets redirectUri rendering errors page view.
	 * 
	 * @param status
	 * @return
	 * @throws TemplateException
	 * @throws IOException
	 */
	private Object getRedirectUriOrRenderErrorView(int status) throws IOException, TemplateException {
		Template tpl = errorTplMappingCache.get(status);
		if (nonNull(tpl)) { // error template?
			return tpl;
		}
		// error redirect URI
		String errorRedirectUri = config.getRenderingMapping().get(status);
		hasText(errorRedirectUri, "No render template or redirection URI found for error status: %s", status);
		return errorRedirectUri.substring(DEFAULT_REDIRECT_PREFIX.length());
	}

	/**
	 * Check redirection error URI.
	 * 
	 * @param uriOrTpl
	 * @return
	 */
	private boolean isErrorRedirectURI(String uriOrTpl) {
		return startsWithIgnoreCase(uriOrTpl, DEFAULT_REDIRECT_PREFIX);
	}

	/**
	 * {@link RenderingErrorHandler}
	 */
	static interface RenderingErrorHandler {

		default Object renderingWithJson(Map<String, Object> model, RespBase<Object> resp) throws Exception {
			throw new UnsupportedOperationException();
		}

		default Object renderingWithView(Map<String, Object> model, int status, String renderString) throws Exception {
			throw new UnsupportedOperationException();
		}

		default Object redirectError(Map<String, Object> model, String errorRedirectURI) throws Exception {
			throw new UnsupportedOperationException();
		}

	}

	final private static String DEFAULT_REDIRECT_PREFIX = "redirect:";
	final private static String DEFAULT_REDIRECT_KEY = "redirectUrl";

}