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

import feign.Body;
import feign.Client;
import feign.Contract;
import feign.Feign;
import feign.FeignException;
import feign.Logger;
import feign.MethodMetadata;
import feign.Request;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import feign.Request.Options;
import feign.Response;
import feign.Retryer;
import feign.Util;
import feign.codec.DecodeException;
import feign.codec.Decoder;
import feign.codec.EncodeException;
import feign.codec.Encoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.slf4j.Slf4jLogger;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.io.CharStreams;
import com.wl4g.component.common.annotation.Reserved;
import com.wl4g.component.common.collection.CollectionUtils2;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.common.web.rest.RespBase;
import com.wl4g.component.rpc.springboot.feign.annotation.mvc.SpringMvcContract;
import com.wl4g.component.rpc.springboot.feign.config.SpringBootFeignAutoConfiguration;
import com.wl4g.component.rpc.springboot.feign.config.SpringBootFeignProperties;
import com.wl4g.component.rpc.springboot.feign.context.FeignContextBinders;
import com.wl4g.component.rpc.springboot.feign.context.RpcContextHolder;

import static com.wl4g.component.common.collection.CollectionUtils2.isEmpty;
import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.lang.Assert2.hasText;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.rpc.springboot.feign.config.SpringBootFeignAutoConfiguration.BEAN_FEIGN_CLIENT;
import static feign.Util.UTF_8;
import static feign.Util.toByteArray;
import static feign.Util.valuesOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
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
@SuppressWarnings("unused")
class SpringBootFeignFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {

	/**
	 * To be consistent with the {@link feign.slf4j.Slf4jLogger} log prefix.
	 */
	private final SmartLogger log = getLogger(feign.Logger.class);

	private ApplicationContext applicationContext;
	private SpringBootFeignProperties config;
	private Client client;
	private Collection<RequestInterceptor> requestInterceptors;

	private Class<T> proxyInterface;
	private String baseUrl;
	@Deprecated
	private String path; // Contract will be append auto
	private boolean decode404;
	private Logger.Level logLevel;
	private Class<?>[] configuration;
	private long connectTimeout;
	private long readTimeout;
	@Deprecated
	private long writeTimeout;
	private boolean followRedirects;

	// Fallback default configuration.
	private Class<?>[] defaultConfiguration;

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	public void setProxyInterface(Class<T> proxyInterface) {
		this.proxyInterface = proxyInterface;
	}

	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}

	public void setPath(String path) {
		this.path = path;
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

	public void setConnectTimeout(long connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public void setReadTimeout(long readTimeout) {
		this.readTimeout = readTimeout;
	}

	public void setWriteTimeout(long writeTimeout) {
		this.writeTimeout = writeTimeout;
	}

	public void setFollowRedirects(boolean followRedirects) {
		this.followRedirects = followRedirects;
	}

	public void setDefaultConfiguration(Class<?>[] defaultConfiguration) {
		this.defaultConfiguration = defaultConfiguration;
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
		// 注意顺序，不能在InitializingBean#afterPropertiesSet()的时候获取bean？
		this.config = obtainFeignConfigProperties();
		this.client = obtainFeignHttpClientInstance();
		this.requestInterceptors = obtainFeignRequestInterceptors();

		// Builder feign
		Feign.Builder builder = Feign.builder().client(client);

		// Sets request interceptors.
		if (!requestInterceptors.isEmpty()) {
			builder.requestInterceptors(requestInterceptors);
		}

		// Sets decode404
		if (decode404) {
			builder.decode404();
		}

		// Sets request option
		mergeRequestOptionSet(builder);

		// Sets logger level
		mergeLoggerLevelSet(builder);

		// Sets configuration with merge.
		mergeConfigurationSet(builder);

		return builder.target(proxyInterface, buildBaseUrl());
	}

	private Collection<RequestInterceptor> obtainFeignRequestInterceptors() {
		if (nonNull(requestInterceptors)) {
			return requestInterceptors;
		}
		try {
			return (requestInterceptors = applicationContext.getBeansOfType(RequestInterceptor.class).values());
		} catch (BeansException e) {
			return emptyList();
		}
	}

	private SpringBootFeignProperties obtainFeignConfigProperties() {
		if (nonNull(config)) {
			return config;
		}
		return (config = applicationContext.getBean(SpringBootFeignProperties.class));
	}

	private Client obtainFeignHttpClientInstance() {
		if (nonNull(client)) {
			return client;
		}
		try {
			client = applicationContext.getBean(BEAN_FEIGN_CLIENT, Client.class);
		} catch (NoSuchBeanDefinitionException e) {
			throw new IllegalStateException("Without one of [okhttp3, Http2Client] client.");
		}
		return client;
	}

	private void mergeConfigurationSet(Feign.Builder builder) throws Exception {
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
		// new GsonEncoder()
		builder.encoder(new DelegateFeignEncoder(isNull(encoder) ? defaultEncoder : encoder));
		// new GsonDecoder()
		// new ParameterizedGsonDecoder()
		builder.decoder(new DelegateFeignDecoder(isNull(decoder) ? defaultDecoder : decoder));
		// new Contract.Default()
		builder.contract(isNull(contract) ? defaultContract : contract);
		// builder.contract(new DelegateContract(isNull(contract)?new
		// SpringMvcContract():contract));
		builder.retryer(isNull(retryer) ? defaultRetryer : retryer);
		builder.logger(isNull(logger) ? defaultLogger : logger);
	}

	private void mergeRequestOptionSet(Feign.Builder builder) {
		long connectTimeout0 = connectTimeout > 0 ? connectTimeout : config.getConnectTimeout();
		long readTimeout0 = readTimeout > 0 ? readTimeout : config.getReadTimeout();
		builder.options(new Options(connectTimeout0, MILLISECONDS, readTimeout0, MILLISECONDS, followRedirects));
	}

	private void mergeLoggerLevelSet(Feign.Builder builder) {
		builder.logLevel((logLevel != Logger.Level.NONE) ? logLevel : config.getDefaultLogLevel());
	}

	private String buildBaseUrl() {
		String _baseUrl = trimToEmpty(isBlank(baseUrl) ? config.getDefaultUrl() : baseUrl);
		hasText(_baseUrl, "Feign base url is required, please check configuration: %s.defaultUrl or use @%s#url()",
				SpringBootFeignAutoConfiguration.KEY_PREFIX, SpringBootFeignClient.class.getSimpleName());
		// Contract will be append auto
		// String path0 = trimToEmpty(path);
		// if (!baseUrl0.endsWith("/") && !path0.startsWith("/")) {
		// baseUrl0 += "/";
		// }
		// return baseUrl0 + path0;
		return _baseUrl;
	}

	class DelegateFeignEncoder implements Encoder {
		private final Encoder encoder;

		public DelegateFeignEncoder(Encoder encoder) {
			this.encoder = notNullOf(encoder, "encoder");
		}

		// Do nothing, Retention extension
		@Override
		public void encode(Object object, Type bodyType, RequestTemplate template) throws EncodeException {
			encoder.encode(object, bodyType, template);
		}
	}

	/**
	 * {@link feign.SynchronousMethodHandler#executeAndDecode()}
	 */
	class DelegateFeignDecoder implements Decoder {
		private final Decoder decoder;

		public DelegateFeignDecoder(Decoder decoder) {
			this.decoder = notNullOf(decoder, "decoder");
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object decode(Response response, Type type) throws IOException, DecodeException, FeignException {
			Response wrapResponse = wrapRepeatableResponseIfNecessary(response);
			try {
				return decoder.decode(wrapResponse, type);
			} catch (Exception e) {
				String errmsg = format("Failed to feign RPC. - return.type: %s,\nhttp.request: %s,\nhttp.response: %s", type,
						printRequestAsString(response.request()), wrapResponse);
				// High concurrency performance optimizing throw exception
				throw new FeignRpcException(errmsg, e, log.isDebugEnabled());
			} finally {
				// The RPC call has responded and the attachment info should be
				// extracted from it.
				try {
					FeignContextBinders.bindFeignResposneAttachmentsToContext(wrapResponse);
				} catch (Exception e2) {
					log.warn("Cannot bind feign response attachments to current RpcContext", e2);
				}
				// Actual close response.
				((RepeatableResponseBody) wrapResponse.body()).actualClose();
			}
		}

		private Response wrapRepeatableResponseIfNecessary(Response response) throws IOException {
			// Wrap response.
			return Response.builder().status(response.status()).reason(response.reason()).request(response.request())
					.headers(response.headers()).body(new RepeatableResponseBody(response.body())).build();
		}

		private String printRequestAsString(Request request) {
			if (request.isBinary()) {
				return request.httpMethod().toString().concat(" ").concat(request.url()).concat(" HTTP/x ").concat(File.separator)
						.concat("--- Binary Data ---");
			}
			return request.toString();
		}
	}

	/**
	 * Delegate wrapper {@link Contract}.
	 */
	@Reserved
	@Deprecated
	class DelegateFeignContract implements Contract {
		private final Contract delegate;

		public DelegateFeignContract(Contract delegate) {
			this.delegate = notNullOf(delegate, "delegate");
		}

		@Override
		public List<MethodMetadata> parseAndValidateMetadata(Class<?> targetType) {
			List<MethodMetadata> mds = delegate.parseAndValidateMetadata(targetType);
			for (MethodMetadata md : safeList(mds)) {
				md.returnType(transformParameterizedType(md.returnType()));
			}
			return mds;
		}

		/**
		 * Wrap transform parameterized raw type to {@link RespBase}
		 */
		private ParameterizedType transformParameterizedType(Type returnType) {
			Type[] actualTypes = { returnType };
			if (returnType instanceof ParameterizedType) {
				ParameterizedType pType = (ParameterizedType) returnType;
				actualTypes = new Type[] { new ParameterizedType() {
					@Override
					public Type getRawType() {
						return pType.getRawType();
					}

					@Override
					public Type getOwnerType() {
						return null;
					}

					@Override
					public Type[] getActualTypeArguments() {
						return pType.getActualTypeArguments();
					}
				} };
			}
			final Type rawType0 = RespBase.class;
			final Type[] actualTypes0 = actualTypes;
			return new ParameterizedType() {

				@Override
				public Type getRawType() {
					return rawType0;
				}

				@Override
				public Type getOwnerType() {
					return null;
				}

				@Override
				public Type[] getActualTypeArguments() {
					return actualTypes0;
				}
			};
		}
	}

	/**
	 * Delegate repeatable {@link InputStream} of {@link feign.Response.Body}
	 */
	class RepeatableResponseBody implements feign.Response.Body {
		private final feign.Response.Body orig;
		private final Reader reader;

		RepeatableResponseBody(feign.Response.Body orig) throws IOException {
			this.orig = notNullOf(orig, "originalBody");
			Reader reader0 = orig.asReader(Util.UTF_8);
			if (!reader0.markSupported()) {
				reader0 = new BufferedReader(reader0, 1) {
					@Override
					public void close() throws IOException {
						// Ignore defer close, see:
						// RepeatableResponseBody#actualClose()
					}
				};
			}
			this.reader = reader0;
		}

		final void actualClose() throws IOException {
			orig.close();
		}

		@Override
		public void close() throws IOException {
			// Ignore defer close, see: RepeatableResponseBody#actualClose()
		}

		@Override
		public Integer length() {
			return orig.length();
		}

		@Override
		public boolean isRepeatable() {
			return true;
		}

		@Override
		public InputStream asInputStream() throws IOException {
			return orig.asInputStream();
		}

		@Override
		public Reader asReader(Charset charset) throws IOException {
			return reader;
		}

		@Override
		public String toString() {
			if (log.isDebugEnabled()) {
				try {
					reader.reset();
					return CharStreams.toString(reader);
				} catch (Exception e) {
					log.error("", e);
				}
			}
			return super.toString();
		}
	}

	public static class FeignRpcException extends RuntimeException {
		static final long serialVersionUID = -7034833390745116939L;

		/**
		 * Has been message to specify whether to log exceptions.
		 * 
		 * @param message
		 * @param cause
		 * @param dumpStackTrace
		 */
		public FeignRpcException(String message, Throwable cause, boolean dumpStackTrace) {
			super(message, cause, false, dumpStackTrace);
		}
	}

	private static final Encoder defaultEncoder = new JacksonEncoder(
			new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL));
	private static final Decoder defaultDecoder = new JacksonDecoder();
	private static final Contract defaultContract = new SpringMvcContract();
	private static final Retryer defaultRetryer = new Retryer.Default();
	private static final Logger defaultLogger = new Slf4jLogger();

}
