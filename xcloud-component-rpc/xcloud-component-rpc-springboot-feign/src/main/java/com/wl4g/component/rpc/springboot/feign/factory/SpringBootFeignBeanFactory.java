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
package com.wl4g.component.rpc.springboot.feign.factory;

import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.wl4g.component.rpc.springboot.feign.config.SpringBootFeignProperties;

import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.component.rpc.springboot.feign.config.SpringBootFeignAutoConfiguration.BEAN_FEIGN_CLIENT;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link SpringBootFeignBeanFactory}
 * 
 * @param <T>
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
public class SpringBootFeignBeanFactory<T> implements FactoryBean<T>, ApplicationContextAware {

	private ApplicationContext applicationContext;
	private Class<T> proxyInterface;
	private String url;
	private boolean decode404;
	private Logger.Level logLevel;
	private Class<?>[] configuration;

	private Class<?>[] defaultConfiguration;
	private Logger.Level defaultLogLevel;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void setProxyInterface(Class<T> proxyInterface) {
		this.proxyInterface = proxyInterface;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setDecode404(boolean decode404) {
		this.decode404 = decode404;
	}

	public void setLogLevel(Logger.Level logLevel) {
		this.logLevel = logLevel;
	}

	public void setConfiguration(Class<?>[] configuration) {
		this.configuration = configuration;
	}

	public void setDefaultConfiguration(Class<?>[] defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
	}

	public void setDefaultLogLevel(Logger.Level defaultLogLevel) {
		this.defaultLogLevel = defaultLogLevel;
	}

	@Override
	public Class<?> getObjectType() {
		return proxyInterface;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@SuppressWarnings({ "deprecation" })
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
		Feign.Builder builder = Feign.builder().client(client).retryer(new Retryer.Default(100, SECONDS.toMillis(1), 0))
				.options(new Request.Options(config.getConnectTimeout(), config.getReadTimeout(), true));
		if (decode404) {
			builder.decode404();
		}
		builder.logLevel((logLevel != Logger.Level.NONE) ? logLevel : defaultLogLevel);

		// Merge configuration
		List<Class<?>> mergedConfiguration = new ArrayList<>(safeArrayToList(configuration));
		mergedConfiguration.addAll(safeArrayToList(defaultConfiguration));
		// If there are multiple configuration classes of the same type, the
		// first one takes effect.
		Encoder encoder = null;
		Decoder decoder = null;
		Contract contract = null;
		for (Class<?> clazz : mergedConfiguration) {
			if (isNull(encoder) && Encoder.class.isAssignableFrom(clazz)) {
				builder.encoder(encoder = (Encoder) clazz.newInstance());
			} else if (isNull(decoder) && Decoder.class.isAssignableFrom(clazz)) {
				builder.decoder(decoder = (Decoder) clazz.newInstance());
			} else if (isNull(contract) && Contract.class.isAssignableFrom(clazz)) {
				builder.contract(contract = (Contract) clazz.newInstance());
			}
		}

		return builder.target(proxyInterface, url);
	}

}
