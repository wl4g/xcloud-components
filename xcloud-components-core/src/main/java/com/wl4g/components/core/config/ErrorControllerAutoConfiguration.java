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
package com.wl4g.components.core.config;

import static com.wl4g.components.common.serialize.JacksonUtils.convertBean;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import com.wl4g.components.core.config.mapping.AbstractHandlerMappingSupport;
import com.wl4g.components.core.config.mapping.PrefixHandlerMapping;
import com.wl4g.components.core.web.error.CompositeErrorConfigurer;
import com.wl4g.components.core.web.error.DefaultErrorConfigurer;
import com.wl4g.components.core.web.error.ErrorConfigurer;
import com.wl4g.components.core.web.error.GlobalErrorController;
import com.wl4g.components.core.web.error.ReactiveSmartErrorHandler;
import com.wl4g.components.core.web.error.ReactiveSmartErrorHandler.ReactiveErrorAttributes;
import com.wl4g.components.core.web.error.ServletSmartErrorController;

/**
 * Smart DevOps error controller auto configuration
 * 
 * @author wangl.sir
 * @version v1.0 2019年1月10日
 * @since
 */
@Configuration
@ConditionalOnProperty(value = "spring.cloud.devops.error.enabled", matchIfMissing = true)
public class ErrorControllerAutoConfiguration extends AbstractHandlerMappingSupport {

	@Bean
	public ErrorConfigurer defaultErrorConfigurer() {
		return new DefaultErrorConfigurer();
	}

	@Bean
	public CompositeErrorConfigurer compositeErrorConfigurer(List<ErrorConfigurer> configures) {
		return new CompositeErrorConfigurer(configures);
	}

	@Bean
	@ConfigurationProperties(prefix = "spring.cloud.devops.error")
	public ErrorHandlerProperties errorControllerProperties() {
		return new ErrorHandlerProperties();
	}

	@Bean
	public PrefixHandlerMapping errorControllerPrefixHandlerMapping() {
		return super.newPrefixHandlerMapping("/", GlobalErrorController.class);
	}

	/**
	 * {@link ReactiveErrorHandlerConfiguration}
	 * 
	 * @see {@link de.codecentric.boot.admin.server.config.AdminServerWebConfiguration.ReactiveRestApiConfiguration}
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
	// @ConditionalOnClass(WebFluxConfigurer.class)
	public static class ReactiveErrorHandlerConfiguration {

		@Bean
		public ReactiveErrorAttributes reactiveErrorAttributes(ErrorHandlerProperties config, CompositeErrorConfigurer adapter) {
			return new ReactiveErrorAttributes();
		}

		/**
		 * @see {@link org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration#errorWebExceptionHandler}
		 */
		@Bean
		@Order(-2) // Takes precedence over the default handler
		public ReactiveSmartErrorHandler reactiveSmartErrorHandler(
				org.springframework.boot.web.reactive.error.ErrorAttributes errorAttributes,
				ResourceProperties resourceProperties, ObjectProvider<ViewResolver> viewResolvers,
				ServerCodecConfigurer codecConfigurer, ApplicationContext applicationContext) {
			ReactiveSmartErrorHandler errorHandler = new ReactiveSmartErrorHandler(errorAttributes, resourceProperties,
					applicationContext);
			errorHandler.setViewResolvers(viewResolvers.orderedStream().collect(toList()));
			errorHandler.setMessageWriters(codecConfigurer.getWriters());
			errorHandler.setMessageReaders(codecConfigurer.getReaders());
			return errorHandler;
		}

	}

	/**
	 * {@link ServletErrorControllerConfirguation}
	 * 
	 * @see {@link de.codecentric.boot.admin.server.config.AdminServerWebConfiguration.ServletRestApiConfirguation}
	 */
	@Configuration(proxyBeanMethods = false)
	@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
	@AutoConfigureAfter(WebMvcAutoConfiguration.class)
	public static class ServletErrorControllerConfirguation {

		@Bean
		public ServletSmartErrorController servletSmartErrorController(ErrorHandlerProperties config, ErrorAttributes errorAttrs,
				CompositeErrorConfigurer adapter) {
			return new ServletSmartErrorController(config, errorAttrs, adapter);
		}

	}

	/**
	 * Error handler controller properties.</br>
	 * <font color=red>Note: When {@link ConfigurationProperties} is used, the
	 * field name cannot contain numbers, otherwise</font>
	 * 
	 * @author Wangl.sir &lt;Wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @version v1.0.0 2019-11-02
	 * @since
	 */
	public static class ErrorHandlerProperties implements InitializingBean {
		final public static String DEFAULT_DIR_VIEW = "/default-error-view/";

		/**
		 * Default error view configuration directory.
		 */
		private String basePath = DEFAULT_DIR_VIEW;

		/**
		 * {@link HttpStatus#NOT_FOUND} error corresponding view template name.
		 */
		private String notFountUriOrTpl = "404.tpl.html";

		/**
		 * {@link HttpStatus#FORBIDDEN} error corresponding view template name.
		 */
		private String unauthorizedUriOrTpl = "403.tpl.html";

		/**
		 * {@link HttpStatus#SERVICE_UNAVAILABLE} error corresponding view
		 * template name.
		 */
		private String errorUriOrTpl = "50x.tpl.html";

		/**
		 * Error return previous page URI.</br>
		 * Default for browser location origin.
		 */
		private String homeUri = "javascript:location.href = location.origin";

		// --- Temporary attribute's. ---

		/**
		 * That convert as map.
		 */
		private Map<String, Object> asMap;

		public String getBasePath() {
			return basePath;
		}

		public void setBasePath(String basePath) {
			if (!isBlank(basePath)) {
				this.basePath = basePath;
			}
		}

		public String getNotFountUriOrTpl() {
			return notFountUriOrTpl;
		}

		public void setNotFountUriOrTpl(String notFountUriOrTpl) {
			if (!isBlank(notFountUriOrTpl)) {
				this.notFountUriOrTpl = notFountUriOrTpl;
			}
		}

		public String getUnauthorizedUriOrTpl() {
			return unauthorizedUriOrTpl;
		}

		public void setUnauthorizedUriOrTpl(String unauthorizedUriOrTpl) {
			if (!isBlank(unauthorizedUriOrTpl)) {
				this.unauthorizedUriOrTpl = unauthorizedUriOrTpl;
			}
		}

		public String getErrorUriOrTpl() {
			return errorUriOrTpl;
		}

		public void setErrorUriOrTpl(String errorUriOrTpl) {
			if (!isBlank(errorUriOrTpl)) {
				this.errorUriOrTpl = errorUriOrTpl;
			}
		}

		public String getHomeUri() {
			return homeUri;
		}

		public void setHomeUri(String homeUri) {
			if (!isBlank(homeUri)) {
				this.homeUri = homeUri;
			}
		}

		// --- Function's. ---

		@SuppressWarnings("unchecked")
		@Override
		public void afterPropertiesSet() throws Exception {
			this.asMap = convertBean(this, HashMap.class);
		}

		/**
		 * Convert bean to {@link Map} properties.
		 * 
		 * @return
		 */

		public Map<String, Object> asMap() {
			return this.asMap;
		}

	}

}