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
package com.wl4g.component.rpc.istio.feign.factory;

import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.Logger;
import feign.Request;
import feign.Retryer;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.component.rpc.istio.feign.config.IstioFeignAutoConfiguration.BEAN_FEIGN_CLIENT;
import com.wl4g.component.rpc.istio.feign.config.IstioFeignProperties;

import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * {@link IstioFeignBeanFactory}
 * 
 * @param <T>
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
public class IstioFeignBeanFactory<T> implements FactoryBean<T>, ApplicationContextAware {

	private ApplicationContext applicationContext;
	private Class<T> proxyInterface;
	private String url;
	private boolean decode404;
	private Logger.Level logLevel;
	private Class<?>[] configuration;

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

	@SuppressWarnings({ "deprecation", "unchecked" })
	@Override
	public T getObject() throws Exception {
		IstioFeignProperties config = applicationContext.getBean(IstioFeignProperties.class);
		Client client;
		try {
			client = applicationContext.getBean(BEAN_FEIGN_CLIENT, Client.class);
		} catch (NoSuchBeanDefinitionException e) {
			throw new IllegalStateException("Without one of [okhttp3, Http2Client] client.");
		}

		// Builder feign
		Feign.Builder builder = Feign.builder().client(client).retryer(new Retryer.Default(100, SECONDS.toMillis(1), 0))
				.options(new Request.Options(config.getConnectTimeout(), config.getReadTimeout(), true)).logLevel(logLevel);

		// Feign codec components
		Class<? extends Encoder> encoder = GsonEncoder.class;
		Class<? extends Decoder> decoder = GsonDecoder.class;
		Class<? extends Contract> contract = Contract.Default.class;
		for (Class<?> clazz : safeArrayToList(configuration)) {
			if (Encoder.class.isAssignableFrom(clazz)) {
				encoder = (Class<? extends Encoder>) clazz;
			} else if (Decoder.class.isAssignableFrom(clazz)) {
				decoder = (Class<? extends Decoder>) clazz;
			} else if (Contract.class.isAssignableFrom(clazz)) {
				contract = (Class<? extends Contract>) clazz;
			}
		}

		if (decode404) {
			builder.decode404();
		}
		if (nonNull(encoder)) {
			builder.encoder(encoder.newInstance());
		}
		if (nonNull(decoder)) {
			builder.decoder(decoder.newInstance());
		}
		if (nonNull(contract)) {
			builder.contract(contract.newInstance());
		}

		return builder.target(proxyInterface, url);
	}

	@Override
	public Class<?> getObjectType() {
		return proxyInterface;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

}
