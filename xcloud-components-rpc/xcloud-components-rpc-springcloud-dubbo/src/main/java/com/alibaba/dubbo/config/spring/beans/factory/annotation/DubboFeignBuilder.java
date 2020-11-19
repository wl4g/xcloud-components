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
package com.alibaba.dubbo.config.spring.beans.factory.annotation;

import com.alibaba.dubbo.config.annotation.Reference;
import feign.Feign;
import feign.Target;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ReflectionUtils;

public class DubboFeignBuilder extends Feign.Builder {

	@Autowired
	private ApplicationContext applicationContext;

	public final Reference defaultReference;

	public DubboFeignBuilder() {
		// 产生@Reference 默认配置实例
		this.defaultReference = ReflectionUtils.findField(DubboFeignBuilder.DefaultReferenceClass.class, "field")
				.getAnnotation(Reference.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T target(Target<T> target) {
		ReferenceBeanBuilder beanBuilder = ReferenceBeanBuilder
				.create(defaultReference, target.getClass().getClassLoader(), applicationContext).interfaceClass(target.type());

		try {
			T object = (T) beanBuilder.build().getObject();
			return object;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	final class DefaultReferenceClass {
		// dubbo早于eureka启动 check设为false 调用时检查
		@Reference(check = false)
		String field;
	}

}