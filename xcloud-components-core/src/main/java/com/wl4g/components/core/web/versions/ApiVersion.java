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
package com.wl4g.components.core.web.versions;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

import org.springframework.web.bind.annotation.Mapping;

/**
 * Multiple versions annotation and control for API.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see
 */
@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Mapping
public @interface ApiVersion {

	/**
	 * The major version number of the API. (Greater than or equal to 0)
	 * 
	 * @return
	 */
	int major();

	/**
	 * The minor version number of the API. (Greater than or equal to 0)
	 * 
	 * @return
	 */
	int minor();

	/**
	 * The revision version number of the API. (Greater than or equal to 0)
	 * 
	 * @return
	 */
	int revision();

	/**
	 * The extension version number of the API. (Optional, The extended version
	 * number is ignored when it is less than 0)
	 * 
	 * @return
	 */
	int extension() default -1;

}
