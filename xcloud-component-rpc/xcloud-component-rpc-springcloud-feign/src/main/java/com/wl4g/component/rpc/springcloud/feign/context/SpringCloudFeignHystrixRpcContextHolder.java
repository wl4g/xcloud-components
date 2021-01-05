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
package com.wl4g.component.rpc.springcloud.feign.context;

import static java.util.Objects.isNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.hystrix.strategy.concurrency.HystrixRequestContext;
import com.netflix.hystrix.strategy.concurrency.HystrixRequestVariableDefault;
import com.wl4g.component.rpc.springboot.feign.annotation.SpringBootFeignClient;
import com.wl4g.component.rpc.springboot.feign.context.RpcContextHolder;

/**
 * Context holder of hytrix in thread isolation mode.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-17
 * @sine v1.0
 * @see
 */
class SpringCloudFeignHystrixRpcContextHolder extends RpcContextHolder implements Closeable {

	private static final HystrixRequestVariableDefault<SpringCloudFeignHystrixRpcContextHolder> LOCAL = new HystrixRequestVariableDefault<>();

	// Notes: Since feignclient ignores case when setting header, it should be
	// unified here.
	/** Feign request context attachments store. */
	private final Map<String, String> attachments = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	@Override
	public String getAttachment(String key) {
		return attachments.get(key);
	}

	@Override
	public Map<String, String> getAttachments() {
		return attachments;
	}

	@Override
	public void setAttachment(String key, String value) {
		attachments.put(key, value);
	}

	@Override
	public void removeAttachment(String key) {
		attachments.remove(key);
	}

	@Override
	public void clearAttachments() {
		attachments.clear();
	}

	@Override
	public void close() throws IOException {
		if (HystrixRequestContext.isCurrentThreadInitialized()) {
			// Destroy current thread
			HystrixRequestContext.getContextForCurrentThread().shutdown();
		}
	}

	@Override
	protected RpcContextHolder current() {
		SpringCloudFeignHystrixRpcContextHolder current = LOCAL.get();
		// Initializes hystrix request context in the current thread.
		if (isNull(current) && !HystrixRequestContext.isCurrentThreadInitialized()) {
			HystrixRequestContext.initializeContext();
			LOCAL.set(current = new SpringCloudFeignHystrixRpcContextHolder());
		}
		return current;
	}

	@Configuration
	@ConditionalOnBean(HytrixFeignContextConfigurer.class)
	@ConditionalOnClass({ SpringBootFeignClient.class, FeignClient.class })
	static class HytrixFeignRpcContextHolderAutoConfiguration {
		@Bean
		public RpcContextHolder hytrixFeignRpcContextHolder() {
			return new SpringCloudFeignHystrixRpcContextHolder();
		}
	}

}
