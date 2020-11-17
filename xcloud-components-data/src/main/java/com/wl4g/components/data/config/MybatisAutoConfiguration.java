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
package com.wl4g.components.data.config;

import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isInterface;
import static java.lang.reflect.Modifier.isPublic;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.aop.ClassFilter;
import org.springframework.aop.MethodMatcher;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.support.AbstractGenericPointcutAdvisor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import com.wl4g.components.core.annotation.condition.ConditionalOnJdwpDebug;
import com.wl4g.components.core.bean.BaseBean;
import com.wl4g.components.data.mybatis.loader.SqlSessionMapperHotspotLoader;
import com.wl4g.components.data.mybatis.loader.SqlSessionMapperHotspotLoader.HotspotLoaderProperties;
import com.wl4g.components.data.mybatis.mapper.GenericBeanMapperInterceptor;

/**
 * {@link SqlSessionMapperHotspotLoader} auto configuration.
 * 
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2019年11月14日
 * @since
 */
@ConditionalOnClass(SqlSessionFactory.class)
public class MybatisAutoConfiguration {

	@Bean
	@ConfigurationProperties(prefix = KEY_MYBATIS_PREFIX)
	public MybatisProperties mybatisProperties() {
		return new MybatisProperties();
	}

	// --- Hotspot loader. ---

	@Bean
	@ConditionalOnJdwpDebug(enableProperty = KEY_HOTSPOT_LOADER_PREFIX + ".enable")
	@ConfigurationProperties(prefix = KEY_HOTSPOT_LOADER_PREFIX)
	// @ConditionalOnBean(SmartSqlSessionFactoryBean.class)
	public HotspotLoaderProperties hotspotLoaderProperties() {
		return new HotspotLoaderProperties();
	}

	@Bean
	@ConditionalOnBean(value = { HotspotLoaderProperties.class })
	public SqlSessionMapperHotspotLoader sqlSessionMapperHotspotLoader(SqlSessionFactoryBean sessionFactory,
			HotspotLoaderProperties config) {
		return new SqlSessionMapperHotspotLoader(sessionFactory, config);
	}

	// --- Bean mapper interceptor. ---

	@Bean
	public GenericBeanMapperInterceptor genericBeanMapperInterceptor() {
		return new GenericBeanMapperInterceptor();
	}

	@Bean
	@ConditionalOnBean(GenericBeanMapperInterceptor.class)
	public PointcutAdvisor genericBeanMapperAspectJExpressionPointcutAdvisor(GenericBeanMapperInterceptor advice) {
		AbstractGenericPointcutAdvisor advisor = new AbstractGenericPointcutAdvisor() {
			private static final long serialVersionUID = 1L;

			@Override
			public Pointcut getPointcut() {
				return new Pointcut() {

					private final List<String> INCLUDE_METHODS = new ArrayList<String>(4) {
						private static final long serialVersionUID = 1L;
						{
							// TODO
						}
					};

					private final List<String> EXCLUDE_METHODS = new ArrayList<String>(4) {
						private static final long serialVersionUID = 1L;
						{
							addAll(asList(Object.class.getDeclaredMethods()).stream().map(m -> m.getName()).collect(toList()));
						}
					};

					@Override
					public MethodMatcher getMethodMatcher() {
						return new MethodMatcher() {

							@Override
							public boolean matches(Method method, Class<?> targetClass) {
								Class<?> declareClass = method.getDeclaringClass();
								int mod = method.getModifiers();
								String name = method.getName();
								return !isAbstract(mod) && isPublic(mod) && !isInterface(declareClass.getModifiers())
										&& !EXCLUDE_METHODS.contains(name) && INCLUDE_METHODS.contains(name);
							}

							@Override
							public boolean isRuntime() {
								return false;
							}

							@Override
							public boolean matches(Method method, Class<?> targetClass, Object... args) {
								throw new Error("Shouldn't be here");
							}
						};
					}

					@Override
					public ClassFilter getClassFilter() {
						return clazz -> {
							return BaseBean.class.isAssignableFrom(clazz) && !isAbstract(clazz.getModifiers())
									&& !isInterface(clazz.getModifiers());
						};
					}
				};
			}
		};
		advisor.setAdvice(advice);
		return advisor;
	}

	final public static String KEY_MYBATIS_PREFIX = "mybatis";
	final public static String KEY_HOTSPOT_LOADER_PREFIX = "spring.cloud.xcloud.components.data.mybatis-loader";

}