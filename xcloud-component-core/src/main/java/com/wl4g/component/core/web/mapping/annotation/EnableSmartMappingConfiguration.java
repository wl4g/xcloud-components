/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.component.core.web.mapping.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Indexed;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.google.common.base.Predicate;

/**
 * {@link EnableSmartHandlerMappingConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-17
 * @sine v1.0
 * @see
 */
@Retention(RUNTIME)
@Target({ TYPE })
@Documented
@Indexed
@Import({ SmartHandlerMappingRegistrar.class })
public @interface EnableSmartMappingConfiguration {

	/**
	 * Refer to {@link #scanBasePackages()}
	 * 
	 * @return
	 */
	@AliasFor(SCAN_BASE_PACKAGE)
	String[] value() default {};

	/**
	 * Excludes the target {@link RequestMappingHandlerMapping} according to the
	 * base scanning packages. </br>
	 * </br>
	 * Notes: if-empty, the all instances that exist in the bean factory
	 * scanned.
	 * 
	 * @see {@link com.wl4g.component.core.web.mapping.WebMvcSmartHandlerMappingConfigurer.SmartServletHandlerMapping#initHandlerMethods()}
	 */
	@AliasFor("value")
	String[] scanBasePackages() default {};

	/**
	 * Include filters, Union with {@link #scanBasePackages()}
	 * 
	 * @return
	 */
	Class<? extends Predicate<Class<?>>>[] includeFilters() default {};

	/**
	 * When the same handler mapping appears, whether to enable overlay in bean
	 * order or not. Default: false
	 * 
	 * @return
	 */
	boolean ambiguousMappingOverrideByOrder() default false;

	/**
	 * Refer: {@link #scanBasePackages()}
	 */
	public static final String INCLUDE_FILTERS = "includeFilters";

	/**
	 * Refer: {@link #scanBasePackages()}
	 */
	public static final String SCAN_BASE_PACKAGE = "scanBasePackages";

	/**
	 * Refer: {@link #ambiguousMappingOverrideByOrder()}
	 */
	public static final String AMBIGUOUS_MAP_OVERRIDE = "ambiguousMappingOverrideByOrder";

}