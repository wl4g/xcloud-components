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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Indexed;

import feign.Logger;
import feign.Retryer;

/**
 * {@link SpringBootFeignClient}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Indexed
@Inherited
@FeignClient // for compatibility with SpringCloud annotation
public @interface SpringBootFeignClient {

	/**
	 * The name of the service with optional protocol prefix. Synonym for
	 * {@link #name() name}. A name must be specified for all clients, whether
	 * or not a url is provided. Can be specified as property key, eg:
	 * ${propertyKey}.
	 * 
	 * @return the name of the service with optional protocol prefix
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "value")
	String value() default "";

	/**
	 * The service id with optional protocol prefix. Synonym for {@link #value()
	 * value}.
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "name")
	String name() default "";

	/**
	 * An absolute base URL or resolvable hostname (the protocol is optional).
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "url")
	String url() default "";

	/**
	 * whether 404s should be decoded instead of throwing FeignExceptions
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "decode404")
	boolean decode404() default false;

	/**
	 * Whether to mark the feign proxy as a primary bean. Defaults to true.
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "primary")
	boolean primary() default true;

	/**
	 * A custom configuration class for the feign client. Can contain override
	 * <code>@Bean</code> definition for the pieces that make up the client, for
	 * instance {@link feign.codec.Decoder}, {@link feign.codec.Encoder},
	 * {@link feign.Contract}, {@link Retryer}, {@link Logger}. </br>
	 *
	 * Default configuration refer to:
	 * {@link EnableSpringBootFeignClients#defaultConfiguration()}
	 * 
	 * @return list of configurations for feign client.
	 */
	@AliasFor(annotation = FeignClient.class, attribute = "configuration")
	Class<?>[] configuration() default {};

	Logger.Level logLevel() default Logger.Level.NONE;

	/**
	 * The request connect timeout(ms)
	 * 
	 * @return
	 */
	long connectTimeout() default -1;

	/**
	 * The request read timeout(ms)
	 * 
	 * @return
	 */
	long readTimeout() default -1;

	/**
	 * The request write timeout(ms)
	 * 
	 * @return
	 */
	@Deprecated
	long writeTimeout() default -1;

	/**
	 * The request support 3xx redirection.
	 * 
	 * @return
	 */
	boolean followRedirects() default true;

}
