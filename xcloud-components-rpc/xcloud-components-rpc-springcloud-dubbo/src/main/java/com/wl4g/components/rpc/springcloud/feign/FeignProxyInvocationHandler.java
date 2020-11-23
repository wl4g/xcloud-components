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
package com.wl4g.components.rpc.springcloud.feign;

import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.context.annotation.Configuration;

import com.alibaba.dubbo.config.spring.ServiceBean;

import static com.wl4g.components.common.collection.Collections2.safeMap;
import static com.wl4g.components.common.lang.Assert2.notNullOf;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.aop.support.AopUtils.isCglibProxy;
import static org.springframework.aop.support.AopUtils.getTargetClass;

import java.lang.reflect.Method;
import java.util.List;

/**
 * {@link FeignProxyInvocationHandler}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @author liqiu
 * @version v1.0 2019-11-20
 * @sine v1.0
 * @see
 */
public class FeignProxyInvocationHandler implements InvocationHandler {

	private final DefaultListableBeanFactory beanFactory;
	private final Class<?> targetInterfaceClass;
	private Object target;

	/**
	 * 如，当前运行在mybatis环境时，由于mapper也是动态生成的，被{@link Configuration} 注释的配置类
	 * {@link org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration}
	 * 晚于{@link FeignProviderProxiesConfigurer}执行, 因此在动态注册本类的时候不能直接从
	 * beanFactory获取实际target实例，只能间接当被调用的时候再获取.
	 * 
	 * </br>
	 * 具体原因分析参考spring源码：{@link org.springframework.context.support.AbstractApplicationContext#refresh()}
	 * 
	 * @param beanFactory
	 * @param targetInterfaceClass
	 */
	public FeignProxyInvocationHandler(DefaultListableBeanFactory beanFactory, Class<?> targetInterfaceClass) {
		this.beanFactory = notNullOf(beanFactory, "beanFactory");
		this.targetInterfaceClass = notNullOf(targetInterfaceClass, "targetInterfaceClass");
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		if (method.getName().equals("toString")) {
			return getTargetClass(proxy).toString();
		}
		if (method.getName().equals("equals")) {
			return method.invoke(getTargetClass(proxy), args);
		}
		if (method.getName().equals("hashCode")) {
			return getTargetClass(proxy).hashCode();
		}

		return method.invoke(getTarget(), args);
	}

	/**
	 * Create and obtain invocation target object.
	 * 
	 * @return
	 */
	protected Object createInvocationTarget() {
		Object targetBean = null;
		try {
			targetBean = beanFactory.getBean(targetInterfaceClass);
		} catch (NoUniqueBeanDefinitionException e) {
			// Fallback, find the original object from multiple
			// beans.
			List<Object> candidateBeans = safeMap(beanFactory.getBeansOfType(targetInterfaceClass)).values().stream()
					.filter(obj -> !isNull(obj) && !(obj instanceof ServiceBean) && !isCglibProxy(obj)).collect(toList());
			if (candidateBeans.size() == 1) {
				targetBean = candidateBeans.get(0);
			} else {
				throw e;
			}
		}
		return targetBean;
	}

	/**
	 * Obtain orig bean instance.
	 * 
	 * @return
	 */
	private final Object getTarget() {
		if (isNull(target)) {
			synchronized (this) {
				if (isNull(target)) {
					this.target = createInvocationTarget();
				}
			}
		}
		return target;
	}

}