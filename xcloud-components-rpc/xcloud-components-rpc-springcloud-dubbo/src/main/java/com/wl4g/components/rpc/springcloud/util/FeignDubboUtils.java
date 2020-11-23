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
package com.wl4g.components.rpc.springcloud.util;

import static org.springframework.util.StringUtils.uncapitalize;

import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.dubbo.config.spring.ServiceBean;
import com.alibaba.dubbo.config.spring.beans.factory.annotation.FeignClientDubboProviderConfigurer;
import com.wl4g.components.rpc.springcloud.feign.FeignProviderProxiesConfigurer;
import com.wl4g.components.rpc.springcloud.feign.FeignProxyController;

/**
 * {@link FeignDubboUtils}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-23
 * @sine v1.0
 * @see
 */
public abstract class FeignDubboUtils {

	/**
	 * Generate feign client proxed bean name.
	 * 
	 * @param interfaceClassName
	 * @return
	 */
	public static String generateFeignProxyBeanName(String interfaceClassName) {
		return uncapitalize(ClassUtils.getShortName(interfaceClassName)) + BEAN_FEIGNPROXY_SUFFIX;
	}

	public static final String BEAN_FEIGNPROXY_SUFFIX = "$".concat(FeignProxyController.class.getSimpleName());

	/**
	 * {@link FeignProviderProxiesConfigurer} must be executed after
	 * {@link FeignClientDubboProviderConfigurer}, because the
	 * {@link RestController} proxy class needs to be created first, On the
	 * contrary, because the latter creates a Dubbo {@link ServiceBean} instance
	 * of the service interface, it is not possible to confirm that the correct
	 * original object is obtained in the former.
	 */
	public static final int BEAN_FEIGNPROXY_ORDER = 9234852;
	public static final int BEAN_FEIGNDUBBO_ORDER = BEAN_FEIGNPROXY_ORDER + 1;

}
