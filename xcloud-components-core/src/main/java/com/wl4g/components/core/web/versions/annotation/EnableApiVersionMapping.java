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
package com.wl4g.components.core.web.versions.annotation;

import org.springframework.context.annotation.Import;

import com.wl4g.components.core.web.versions.AsciiVersionComparator;

import java.lang.annotation.*;
import java.util.Comparator;

/**
 * When enabled, the API multi version request control mapping processor is
 * automatically configured.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-27
 * @sine v1.0
 * @see
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import({ ApiVersionMappingRegistrar.class })
public @interface EnableApiVersionMapping {

	/**
	 * Request parameter name for multi version mappings.
	 * 
	 * @return
	 */
	String[] parameterNames() default { "api-version", "api_version", "_v" };

	/**
	 * Version number size comparator for multi version automatic mapping. (bean
	 * type)
	 * 
	 * @return
	 */
	Class<? extends Comparator<String>> comparator() default AsciiVersionComparator.class;

}