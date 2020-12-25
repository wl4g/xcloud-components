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
package com.wl4g.component.rpc.springboot.feign.annotation;

import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.Logger;
import feign.Request.Options;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.slf4j.Slf4jLogger;
import lombok.Getter;
import lombok.Setter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.wl4g.component.rpc.springboot.feign.annotation.mvc.SpringMvcContract;
import com.wl4g.component.rpc.springboot.feign.config.SpringBootFeignAutoConfiguration;
import com.wl4g.component.rpc.springboot.feign.config.SpringBootFeignProperties;

import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.component.common.lang.Assert2.hasText;
import static com.wl4g.component.rpc.springboot.feign.config.SpringBootFeignAutoConfiguration.BEAN_FEIGN_CLIENT;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link SpringBootFeignFactoryBean}
 * 
 * @param <T>
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
@Getter
@Setter
class SpringBootFeignFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {

	private ApplicationContext applicationContext;
	private Class<T> proxyInterface;
	private String baseUrl;
	private String path; // handler mapping path(type)
	private boolean decode404;
	private Logger.Level logLevel;
	private Class<?>[] configuration;
	private long connectTimeout;
	private long readTimeout;
	private long writeTimeout; // Ignore
	private boolean followRedirects;

	// Fallback default configuration.
	private Class<?>[] defaultConfiguration;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public Class<?> getObjectType() {
		return proxyInterface;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public T getObject() throws Exception {
		SpringBootFeignProperties config = applicationContext.getBean(SpringBootFeignProperties.class);
		Client client;
		try {
			client = applicationContext.getBean(BEAN_FEIGN_CLIENT, Client.class);
		} catch (NoSuchBeanDefinitionException e) {
			throw new IllegalStateException("Without one of [okhttp3, Http2Client] client.");
		}

		// Builder feign
		Feign.Builder builder = Feign.builder().client(client);

		// Sets decode404
		if (decode404) {
			builder.decode404();
		}

		// Sets request option
		mergeRequestOptionSet(config, builder);

		// Sets logger/level
		mergeLoggerLevelSet(config, builder);

		// Sets configuration with merge.
		mergeConfigurationSet(config, builder);

		return builder.target(proxyInterface, buildUrl(config));
	}

	private void mergeConfigurationSet(SpringBootFeignProperties config, Feign.Builder builder) throws Exception {
		List<Class<?>> mergedConfiguration = new ArrayList<>(safeArrayToList(configuration));
		mergedConfiguration.addAll(safeArrayToList(defaultConfiguration));
		Encoder encoder = null;
		Decoder decoder = null;
		Contract contract = null;
		Retryer retryer = null;
		Logger logger = null;
		for (Class<?> clazz : mergedConfiguration) {
			// If there are multiple configuration classes of the same type, the
			// first one takes effect.
			if (isNull(encoder) && Encoder.class.isAssignableFrom(clazz)) {
				encoder = (Encoder) clazz.newInstance();
			} else if (isNull(decoder) && Decoder.class.isAssignableFrom(clazz)) {
				decoder = (Decoder) clazz.newInstance();
			} else if (isNull(contract) && Contract.class.isAssignableFrom(clazz)) {
				contract = (Contract) clazz.newInstance();
			} else if (isNull(retryer) && Retryer.class.isAssignableFrom(clazz)) {
				retryer = (Retryer) clazz.newInstance();
			} else if (isNull(logger) && Logger.class.isAssignableFrom(clazz)) {
				logger = (Logger) clazz.newInstance();
			} else {
				throw new IllegalArgumentException(
						format("Unsupported spring boot feign configuration type: %s, The supported lists are: %s, %s, %s, %s",
								clazz, Encoder.class, Decoder.class, Contract.class, Retryer.class, Logger.class));
			}
		}
		builder.encoder(isNull(encoder) ? new GsonEncoder() : encoder);
		builder.decoder(isNull(decoder) ? new GsonDecoder() : decoder);
		// new Contract.Default()
		builder.contract(isNull(contract) ? new SpringMvcContract() : contract);
		builder.retryer(isNull(retryer) ? new DefaultRetryer() : retryer);
		builder.logger(isNull(logger) ? new Slf4jLogger() : logger);
	}

	private void mergeRequestOptionSet(SpringBootFeignProperties config, Feign.Builder builder) {
		long connectTimeout0 = connectTimeout > 0 ? connectTimeout : config.getConnectTimeout();
		long readTimeout0 = readTimeout > 0 ? readTimeout : config.getReadTimeout();
		builder.options(new Options(connectTimeout0, MILLISECONDS, readTimeout0, MILLISECONDS, followRedirects));
	}

	private void mergeLoggerLevelSet(SpringBootFeignProperties config, Feign.Builder builder) {
		builder.logLevel((logLevel != Logger.Level.NONE) ? logLevel : config.getDefaultLogLevel());
	}

	private String buildUrl(SpringBootFeignProperties config) {
		String baseUrl0 = trimToEmpty(isBlank(baseUrl) ? config.getDefaultUrl() : baseUrl);
		hasText(baseUrl0, "feign base url is required, please check configuration: %s.defaultUrl or use @%s#url()",
				SpringBootFeignAutoConfiguration.KEY_PREFIX, SpringBootFeignClient.class.getSimpleName());

		// String path0 = trimToEmpty(path);
		// if (!baseUrl0.endsWith("/") && !path0.startsWith("/")) {
		// baseUrl0 += "/";
		// }
		// return baseUrl0 + path0;

		return baseUrl0;
	}

	public static class DefaultRetryer extends Retryer.Default {
		public DefaultRetryer() {
			super(100, SECONDS.toMillis(1), 0);
		}
	}

}
