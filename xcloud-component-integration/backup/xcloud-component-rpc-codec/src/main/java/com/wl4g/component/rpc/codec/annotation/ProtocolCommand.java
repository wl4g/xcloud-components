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
package com.wl4g.component.integration.codec.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * {@link ProtocolCommand}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-14
 * @sine v1.0
 * @see
 */
@Documented
@Retention(RUNTIME)
@Target(TYPE)
@Inherited
public @interface ProtocolCommand {

	/**
	 * Alias to {@link #commandId()}
	 * 
	 * @return
	 */
	int value();

	/**
	 * Protocol message type command.
	 * 
	 * @return
	 */
	int commandId();

	/**
	 * Protocol message type description.
	 * 
	 * @return
	 */
	String description() default "";

}
