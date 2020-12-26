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

import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import com.wl4g.component.core.web.mapping.annotation.EnableSmartMappingConfiguration;
import com.wl4g.component.core.web.mapping.annotation.EnableSmartMappingConfiguration.DefaultHandlerFilter;
import com.wl4g.component.rpc.springboot.feign.config.SpringBootFeignAutoConfiguration;
import com.wl4g.component.rpc.springboot.feign.context.interceptor.FeignContextServletConfigurer;

import static com.wl4g.component.common.lang.ClassUtils2.getPackageName;
import static com.wl4g.component.rpc.springboot.feign.annotation.EnableSpringBootFeignClients.ExcludeFeignClientsHandlerFilter;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.startsWithAny;

import feign.Logger;
import feign.Retryer;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * {@link EnableSpringBootFeignClients}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-23
 * @sine v1.0
 * @see
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@EnableSmartMappingConfiguration(includeFilters = ExcludeFeignClientsHandlerFilter.class)
@Import({ SpringBootFeignAutoConfiguration.class, SpringBootFeignClientsRegistrar.class, FeignContextServletConfigurer.class })
public @interface EnableSpringBootFeignClients {

	@AliasFor(SCAN_BASE_PACKAGES)
	String[] value() default {};

	/**
	 * Base packages to scan for annotated components.
	 * 
	 * @return
	 */
	@AliasFor("value")
	String[] scanBasePackages() default {};

	/**
	 * Base packages to scan for annotated components.
	 * 
	 * @return
	 */
	Class<?>[] scanBasePackageClasses() default {};

	/**
	 * The default custom <code>@Configuration</code> for all feign clients. Can
	 * contain override <code>@Bean</code> definition for the pieces that make
	 * up the client, for instance {@link feign.codec.Decoder},
	 * {@link feign.codec.Encoder}, {@link feign.Contract}, {@link Retryer},
	 * {@link Logger}. </br>
	 * </br>
	 * 
	 * The if empty, default refer to
	 * {@link SpringBootFeignFactoryBean#mergeFeignConfigurationSet()}
	 * 
	 * @see FeignClientsConfiguration for the defaults
	 * @return list of default configurations
	 */
	Class<?>[] defaultConfiguration() default {};

	/**
	 * Refer: {@link #scanBasePackages()}
	 */
	public static final String SCAN_BASE_PACKAGES = "scanBasePackages";

	/**
	 * Refer: {@link #scanBasePackageClasses()}
	 */
	public static final String SCAN_BASE_PACKAGE_CLASSES = "scanBasePackageClasses";

	/**
	 * Refer: {@link #defaultConfiguration()}
	 */
	public static final String DEFAULT_CONFIGURATION = "defaultConfiguration";

	public static class ExcludeFeignClientsHandlerFilter extends DefaultHandlerFilter {
		private static String[] scanBasePackages = {};

		public static void setScanBasePackages(String[] scanBasePackages) {
			if (nonNull(scanBasePackages)) {
				ExcludeFeignClientsHandlerFilter.scanBasePackages = scanBasePackages;
			}
		}

		@Override
		public boolean apply(@Nullable Class<?> beanType) {
			// 排除被 @SpringBootFeignClient 包含的接口，如，service(facade)层启动，需注入
			// data(dao) 层的feign实例这个场景.
			return !startsWithAny(getPackageName(beanType), scanBasePackages) && super.apply(beanType);
		}
	}

}
