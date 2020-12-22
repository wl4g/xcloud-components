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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

import com.wl4g.component.common.lang.period.PeriodFormatter;
import com.wl4g.component.common.web.rest.RespBase;
import com.wl4g.component.core.bean.BaseBean;
import com.wl4g.component.core.web.method.HandlerMethodCustomizerInterceptor.HandlerProcessor;

/**
 * {@link HumanDateConversionProcessor}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-21
 * @sine v1.0
 * @see
 */
public class HumanDateConversionProcessor implements HandlerProcessor {

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 100;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void postHandle(@NotNull Object bean, @NotNull Method method, Object[] parameters, Object result) {
		if (nonNull(result)) {
			if (result instanceof RespBase) {
				Object data = ((RespBase) result).getData();
				convertIfNecessary(data);
			} else {
				convertIfNecessary(result);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static void convertIfNecessary(Object data) {
		if (data instanceof BaseBean) {
			processHumanIfNecessary(data);
		} else if (data instanceof Collection) {
			Collection<Object> elements = (Collection<Object>) data;
			for (Object element : elements) {
				processHumanIfNecessary(element);
			}
		} else if (data instanceof Map) {
			Map<Object, Object> elements = (Map<Object, Object>) data;
			elements.forEach((key, value) -> {
				processHumanIfNecessary(key);
				processHumanIfNecessary(value);
			});
		}
	}

	/**
	 * Process model date attribute to human format.
	 * 
	 * @param model
	 * @return
	 */
	public static boolean processHumanIfNecessary(Object model) {
		if (model instanceof BaseBean) {
			BaseBean bean = (BaseBean) model;
			if (!isNull(bean) && (isNull(bean.getHumanCreateDate()) || isNull(bean.getHumanCreateDate()))) {
				bean.setHumanCreateDate(
						isNull(bean.getCreateDate()) ? null : defaultFormatter.formatHumanDate(bean.getCreateDate().getTime()));
				bean.setHumanUpdateDate(
						isNull(bean.getUpdateDate()) ? null : defaultFormatter.formatHumanDate(bean.getUpdateDate().getTime()));
			}
			return true;
		}
		return false;
	}

	/*
	 * Human date formatter instance.
	 */
	private static final PeriodFormatter defaultFormatter = PeriodFormatter.getDefault().ignoreLowerDate(true);

	@Configuration
	static class HumanDateConversionAutoConfiguration {
		@Bean
		public HumanDateConversionProcessor humanDateConversion() {
			return new HumanDateConversionProcessor();
		}
	}

}
