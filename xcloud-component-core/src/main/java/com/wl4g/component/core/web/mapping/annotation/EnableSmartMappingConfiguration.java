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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Indexed;
import org.springframework.web.bind.annotation.RequestMapping;

import static org.springframework.core.annotation.AnnotatedElementUtils.hasAnnotation;

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
	 * Refer to {@link #basePackages()}
	 * 
	 * @return
	 */
	@AliasFor(FILTERS)
	Class<? extends Predicate<Class<?>>>[] value() default { DefaultMappingHandlerFilter.class };

	/**
	 * Method mapping handler filters.
	 * 
	 * @return
	 */
	Class<? extends Predicate<Class<?>>>[] filters() default { DefaultMappingHandlerFilter.class };

	/**
	 * When the same handler mapping appears, whether to enable overlay in bean
	 * order or not. Default: false
	 * 
	 * @return
	 */
	boolean overrideAmbiguousByOrder() default true;

	/**
	 * Refer: {@link #basePackages()}
	 */
	public static final String FILTERS = "filters";

	/**
	 * Refer: {@link #overrideAmbiguousByOrder()}
	 */
	public static final String OVERRIDE_AMBIGUOUS = "overrideAmbiguousByOrder";

	public static class DefaultMappingHandlerFilter implements Predicate<Class<?>> {
		@Override
		public boolean apply(@Nullable Class<?> beanType) {
			return hasAnnotation(beanType, Controller.class) || hasAnnotation(beanType, RequestMapping.class);
		}
	}

}
