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
package com.wl4g.component.core.web.method.annotation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import com.google.common.annotations.Beta;

/**
 * {@link HandlerMappingMethodInterceptorRegistrar}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-17
 * @sine v1.0
 * @see
 */
@Retention(RUNTIME)
@Target({ TYPE })
@Documented
@Import({ HandlerMappingMethodInterceptorRegistrar.class })
@Beta
public @interface EnableHandlerMappingCustomizer {

	/**
	 * {@link #basePackages()}
	 * 
	 * @return
	 */
	@AliasFor(BASE_PACKAGES)
	String[] value() default {};

	/**
	 * Smart customizer handler mapping base packages.
	 * 
	 * @return
	 */
	@AliasFor("value")
	String[] basePackages() default {};

	/**
	 * Base packages to scan for annotated components.
	 * 
	 * @return
	 */
	Class<?>[] basePackageClasses() default {};

	/**
	 * Refer: {@link #value()}
	 */
	public static final String BASE_PACKAGES = "basePackages";

	/**
	 * Refer: {@link #basePackageClasses()}
	 */
	public static final String BASE_PACKAGE_CLASSES = "basePackageClasses";

}