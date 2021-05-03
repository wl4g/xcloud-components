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
package com.wl4g.component.data.cache.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Indexed;

/**
 * Enable cache for invalidate methods of any type.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-05-02
 * @sine v1.0
 * @see
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
@Indexed
@Inherited
public @interface CacheInvalidate {

	/**
	 * Sets method cache name.</br>
	 * 
	 * <pre>
	 * for example: name="usercache:"
	 * </pre>
	 */
	String name() default DEFAULT_CACHE_NAME;

	/**
	 * Sets method cache key.</br>
	 * 
	 * <pre>
	 * for example: key="#user.userId"
	 * </pre>
	 */
	String key() default "";

	/**
	 * Default cache name.
	 */
	public static final String DEFAULT_CACHE_NAME = "cached:";

}