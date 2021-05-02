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
package com.wl4g.component.data.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enable cache for query methods of any type.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-05-02
 * @sine v1.0
 * @see
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
public @interface DataCache {

	/**
	 * Sets method cache prefix.</br>
	 * supported spring environment expression.
	 */
	String prefix() default DEFAULT_CACHE_KEY_PREFIX;

	/**
	 * Sets method caching expire time(ms).</br>
	 * supported spring environment expression.
	 */
	String expireMs() default DEFAULT_CACHE_EXPIRE_MS + "";

	/**
	 * Default query cache prefix.
	 */
	public static final String DEFAULT_CACHE_KEY_PREFIX = "querycache:";

	/**
	 * Default query cache expireMs.
	 */
	public static final long DEFAULT_CACHE_EXPIRE_MS = 60 * 1000;

	@Getter
	@AllArgsConstructor
	public static class DataCacheWrapper {
		private String prefix;
		private long expireMs;
	}

}