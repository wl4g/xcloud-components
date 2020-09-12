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
import static com.wl4g.components.common.lang.Assert2.notNull;
import static com.wl4g.components.common.lang.Assert2.notNullOf;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.startsWithIgnoreCase;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.common.view.FreemarkerUtils;
import com.wl4g.components.core.config.ErrorControllerAutoConfiguration.ErrorHandlerProperties;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * {@link ErrorHelper}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-12
 * @sine v1.0.0
 * @see
 */
public class ErrorHelper implements InitializingBean {

	final protected SmartLogger log = getLogger(getClass());

	/** Errors configuration properties. */
	final protected ErrorHandlerProperties config;

	/** Errors configurer. */
	final protected CompositeErrorConfigurer configurer;

	private Template tpl404;
	private Template tpl403;
	private Template tpl50x;

	public ErrorHelper(ErrorHandlerProperties config, CompositeErrorConfigurer configurer) {
		this.config = notNullOf(config, "config");
		this.configurer = notNullOf(configurer, "configurer");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		log.info("Initializing smart global error controller ...");

		try {
			// Initial errors template.
			Configuration fmc = FreemarkerUtils.createDefaultConfiguration(null);
			if (!isErrorRedirectURI(config.getNotFountUriOrTpl())) {
				this.tpl404 = fmc.getTemplate(config.getNotFountUriOrTpl(), UTF_8.name());
				notNull(tpl404, "Default 404 view template must not be null");
			}
			if (!isErrorRedirectURI(config.getUnauthorizedUriOrTpl())) {
				this.tpl403 = fmc.getTemplate(config.getUnauthorizedUriOrTpl(), UTF_8.name());
				notNull(tpl403, "Default 403 view template must not be null");
			}
			if (!isErrorRedirectURI(config.getErrorUriOrTpl())) {
				this.tpl50x = fmc.getTemplate(config.getErrorUriOrTpl(), UTF_8.name());
				notNull(tpl50x, "Default 500 view template must not be null");
			}
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Get redirectUri rendering errors page view.
	 * 
	 * @param model
	 * @param status
	 * @return
	 * @throws TemplateException
	 * @throws IOException
	 */
	public Object getRedirectUriOrRenderErrorView(Map<String, Object> model, int status) throws IOException, TemplateException {
		switch (status) {
		case 404:
			if (nonNull(tpl404)) {
				return tpl404;
			}
			return config.getNotFountUriOrTpl().substring(DEFAULT_REDIRECT_PREFIX.length());
		case 403:
			if (nonNull(tpl403)) {
				return tpl403;
			}
			return config.getUnauthorizedUriOrTpl().substring(DEFAULT_REDIRECT_PREFIX.length());
		default:
			if (nonNull(tpl50x)) {
				return tpl50x;
			}
			return config.getErrorUriOrTpl().substring(DEFAULT_REDIRECT_PREFIX.length());
		}
	}

	/**
	 * Is redirection error URI.
	 * 
	 * @param uriOrTpl
	 * @return
	 */
	private boolean isErrorRedirectURI(String uriOrTpl) {
		return startsWithIgnoreCase(uriOrTpl, DEFAULT_REDIRECT_PREFIX);
	}

	final private static String DEFAULT_REDIRECT_PREFIX = "redirect:";

}
