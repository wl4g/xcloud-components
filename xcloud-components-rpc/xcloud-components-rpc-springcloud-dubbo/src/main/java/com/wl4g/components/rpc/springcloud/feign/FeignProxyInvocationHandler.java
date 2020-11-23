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
import static org.springframework.aop.support.AopUtils.getTargetClass;

import com.alibaba.dubbo.config.spring.ServiceBean;

import com.wl4g.components.common.lang.ClassUtils2;
import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.core.utils.AopUtils2;

import static com.wl4g.components.common.collection.Collections2.safeMap;
import static com.wl4g.components.common.lang.Assert2.notNullOf;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;

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

	protected final SmartLogger log = getLogger(getClass());

	private final DefaultListableBeanFactory beanFactory;
	private final Class<?> targetInterfaceClass;
	private Object target;

	/**
	 * 如，当前运行在mybatis环境中时，由于mapper实例是动态生成的，且被{@link Configuration}注释的配置类
	 * {@link org.mybatis.spring.boot.autoconfigure.MybatisAutoConfiguration}
	 * 晚于{@link FeignProviderProxiesConfigurer}执行, 因此在动态注册本类的时候不能直接调用
	 * beanFactory.getBean(..)来获取target实例，只能间接当被调用的时候再获取.
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
		return method.invoke(getOrigTarget(), args);
	}

	/**
	 * Obtain invocation orig target object.
	 * 
	 * @return
	 */
	private final Object getOrigTarget() {
		if (isNull(target)) {
			synchronized (this) {
				if (isNull(target)) {
					try {
						this.target = beanFactory.getBean(targetInterfaceClass);
					} catch (NoUniqueBeanDefinitionException e) {
						// Fallback, find the original object from multiple
						// beans.
						this.target = getBestCandidateOrigTarget(
								safeMap(beanFactory.getBeansOfType(targetInterfaceClass)).values());
						if (isNull(this.target)) {
							throw e;
						}
					}
				}
			}
		}
		return target;
	}

	/**
	 * Gets best candidates orig target. </br>
	 * </br>
	 * <p>
	 * Filter excluded objects:</br>
	 * 1. Servicebean proxy instance of provider in Dubbo environment;</br>
	 * 2. The rest controller proxy instance of feign client in springcloud +
	 * Dubbo environment;</br>
	 * </br>
	 * Filter included objects:</br>
	 * 1. Mapper proxy instance in mybatis environment;</br>
	 * </p>
	 * 
	 * @param candidateBeans
	 * @return
	 */
	protected Object getBestCandidateOrigTarget(Collection<?> candidateBeans) {
		List<Object> candidates = candidateBeans.stream()
				.filter(obj -> (nonNull(obj) && !(obj instanceof ServiceBean) && !(obj instanceof FeignProxyController)
						|| (nonNull(mapperProxyClass) && mapperProxyClass.isInstance(AopUtils2.getTarget(obj)))))
				.collect(toList());
		if (candidates.size() == 1) {
			Object best = candidates.get(0);
			log.info("Using best candidate bean: {} of targetInterfaceClass: {}, candidates: {}", best, targetInterfaceClass,
					candidateBeans);
			return best;
		} else {
			log.warn("Not found matchs best candidate bean of targetInterfaceClass: {}, candidates: {}", targetInterfaceClass,
					candidateBeans);
			return null;
		}
	}

	/**
	 * Mybatis mapper proxy class.
	 */
	private static final Class<?> mapperProxyClass = ClassUtils2.resolveClassNameOrNull("org.apache.ibatis.binding.MapperProxy");

}