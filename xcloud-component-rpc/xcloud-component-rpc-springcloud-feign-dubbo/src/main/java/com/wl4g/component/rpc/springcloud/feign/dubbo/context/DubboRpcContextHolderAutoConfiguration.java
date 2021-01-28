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
package com.wl4g.component.rpc.springcloud.feign.dubbo.context;

import java.util.Map;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import com.alibaba.boot.dubbo.autoconfigure.DubboAutoConfiguration;
import com.alibaba.dubbo.rpc.RpcContext;
import com.wl4g.component.rpc.springboot.feign.context.RpcContextHolder;

/**
 * {@link DubboRpcContextHolderAutoConfiguration}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-28
 * @sine v1.0
 * @see
 */
@ConditionalOnClass(DubboAutoConfiguration.class)
public class DubboRpcContextHolderAutoConfiguration {

	@Bean
	public RpcContextHolder springCloudDubboRpcContextHolder() {
		return new SpringCloudDubboRpcContextHolder();
	}

	/**
	 * {@link SpringCloudDubboRpcContextHolder}
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @version v1.0 2020-12-17
	 * @sine v1.0
	 * @see
	 */
	static class SpringCloudDubboRpcContextHolder extends RpcContextHolder {

		@Override
		public String getAttachment(String key) {
			return RpcContext.getContext().getAttachment(key);
		}

		@Override
		public Map<String, String> getAttachments() {
			return RpcContext.getContext().getAttachments();
		}

		@Override
		public void setAttachment(String key, String value) {
			RpcContext.getContext().setAttachment(key, value);
		}

		@Override
		public void removeAttachment(String key) {
			RpcContext.getContext().removeAttachment(key);
		}

		@Override
		public void clearAttachments() {
			RpcContext.getContext().clearAttachments();
		}

		@Override
		protected RpcContextHolder current() {
			return this;
		}

	}

}
