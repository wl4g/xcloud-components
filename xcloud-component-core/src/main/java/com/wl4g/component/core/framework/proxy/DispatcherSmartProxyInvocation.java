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
package com.wl4g.component.core.framework.proxy;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;

import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.reflect.ReflectionUtils2.makeAccessible;
import static java.util.Objects.nonNull;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Supplier;

/**
 * {@link DispatcherSmartProxyInvocation}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author liqiu
 * @version v1.0 2019-11-20
 * @sine v1.0
 * @see
 */
public class DispatcherSmartProxyInvocation extends AbstractDispatcherProxyInvocation {

	protected final Supplier<Object> targetSuplier;
	protected Object target; // typeof object(not FactoryBean)

	public DispatcherSmartProxyInvocation(DefaultListableBeanFactory beanFactory, SmartProxyAutoConfiguration configurer,
			String targetBeanName, Class<?> targetClass, Supplier<Object> targetSuplier) {
		super(beanFactory, configurer, targetBeanName, targetClass);
		this.targetSuplier = notNullOf(targetSuplier, "targetSuplier");
	}

	@Override
	public Object getTarget() {
		// TODO use cache?
		// e.g: but org.mybatis.spring.MapperFactoryBean#getObject()
		// Returns a new object each time.
		return (target = targetSuplier.get());
		/**
		 * Notes: do not use {@link BeanFactory#getBean(targetBeanName)} to get
		 * the actual original object here, because it may have been overridden
		 * and registered as a proxy object, and there will be a dead loop when
		 * calling.
		 */
	}

	@Override
	public Object doInvoke(Object target, Method method, Object[] args) throws Throwable {
		if (!method.isAccessible()) {
			makeAccessible(method);
		}

		// Gets proxies handlers.
		List<SmartProxyInterceptor> processors = configurer.getProcessors(targetClass);

		// Prepared process
		for (SmartProxyInterceptor p : processors) {
			if (p.supportMethodProxy(target, method, targetClass, args)) {
				args = p.preHandle(target, method, args);
			}
		}

		// Invoke actual method.
		Throwable ex = null;
		Object result = null;
		try {
			result = method.invoke(target, args);
		} catch (Throwable e) {
			ex = e;
		}

		// Post process
		for (SmartProxyInterceptor p : processors) {
			if (p.supportMethodProxy(target, method, targetClass, args)) {
				result = p.postHandle(target, method, args, result, ex);
			}
		}

		// Throws actual invoke exception.
		if (nonNull(ex)) { // TODO remove
			throw ex;
		}

		log.debug("Invoked enhance proxy orig method: {}, args:{}, return: {}", method, args, result);
		return result;
	}

}