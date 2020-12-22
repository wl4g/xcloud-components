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
package com.wl4g.component.core.web.method;

import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;

import com.wl4g.component.common.collection.CollectionUtils2;

/**
 * It is used to process the subsequent processing of the controller method's
 * return, such as converting the date into a preferred format according to the
 * user's language and habits.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-21
 * @sine v1.0
 * @see
 */
public class HandlerMethodCustomizerInterceptor implements MethodInterceptor {

	private ListableBeanFactory beanFactory;

	private final AtomicBoolean init = new AtomicBoolean(false);
	private volatile List<HandlerProcessor> conversions;

	public void setBeanFactory(ListableBeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public Object invoke(MethodInvocation invo) throws Throwable {
		// Prepared process
		if (nonNull(obtainOrInitConversions())) {
			obtainOrInitConversions().forEach(c -> c.preHandle(invo.getThis(), invo.getMethod(), invo.getArguments()));
		}

		// Invoke actual method
		Object result = invo.proceed();

		// Post process
		if (nonNull(obtainOrInitConversions())) {
			obtainOrInitConversions().forEach(c -> c.postHandle(invo.getThis(), invo.getMethod(), invo.getArguments(), result));
		}

		return result;
	}

	/**
	 * Obtain {@link HandlerProcessor} conversion instances.
	 * 
	 * @return
	 */
	private final List<HandlerProcessor> obtainOrInitConversions() {
		if (isNull(conversions) && init.compareAndSet(false, true)) {
			synchronized (this) {
				if (isNull(conversions)) {
					// Obtain handler conversions.
					List<HandlerProcessor> conversions0 = safeMap(beanFactory.getBeansOfType(HandlerProcessor.class)).values()
							.stream().collect(toList());

					if (!CollectionUtils2.isEmpty(conversions0)) {
						// By order priority
						AnnotationAwareOrderComparator.sort(conversions0);
						this.conversions = conversions0;
					}
				}
			}
		}
		return conversions;
	}

	/**
	 * Request mapping handler processor.
	 */
	public static interface HandlerProcessor extends Ordered {
		default void preHandle(@NotNull Object bean, @NotNull Method method, @Nullable Object[] parameters) {
		}

		default void postHandle(@NotNull Object bean, @NotNull Method method, @Nullable Object[] parameters,
				@Nullable Object result) {
		}
	}

}
