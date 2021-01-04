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
package com.wl4g.component.core.annotation.condition;

import static com.wl4g.component.common.jvm.JvmRuntimeKit.isJVMDebugging;
import static com.wl4g.component.common.lang.Assert2.isTrue;
import static com.wl4g.component.core.annotation.condition.ConditionalOnJdwpDebug.OnJdwpDebugCondition;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Check whether the current JVM process is started in the debugging mode of
 * jdwp, otherwise the condition is not tenable.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2019年12月26日
 * @since
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Documented
@Conditional(OnJdwpDebugCondition.class)
public @interface ConditionalOnJdwpDebug {

	/**
	 * The key-name of the enabled environment configuration attribute property.
	 * 
	 * @return
	 */
	String enableProperty();

	/**
	 * Must be consistent with:
	 * {@link com.wl4g.component.core.annotation.condition.ConditionalOnJdwpDebug#enableProperty}
	 */
	public static final String ENABLE_PROPERTY = "enableProperty";

	/**
	 * Check whether the current JVM process is started in the debugging mode of
	 * jdwp, otherwise the condition is not tenable.
	 * {@link ConditionalOnJdwpDebug}
	 * 
	 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
	 * @version v1.0 2019年12月26日
	 * @since
	 */
	@Order(Ordered.HIGHEST_PRECEDENCE + 50)
	static class OnJdwpDebugCondition implements Condition {
		@Override
		public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
			Object enablePropertyName = metadata.getAnnotationAttributes(ConditionalOnJdwpDebug.class.getName())
					.get(ENABLE_PROPERTY);
			isTrue(nonNull(enablePropertyName) && isNotBlank(enablePropertyName.toString()),
					format("%s.%s It shouldn't be empty", ConditionalOnJdwpDebug.class.getSimpleName(), ENABLE_PROPERTY));

			// Obtain environment enable property value.
			Boolean enable = context.getEnvironment().getProperty(enablePropertyName.toString(), Boolean.class);
			return isNull(enable) ? isJVMDebugging : enable;
		}
	}

}