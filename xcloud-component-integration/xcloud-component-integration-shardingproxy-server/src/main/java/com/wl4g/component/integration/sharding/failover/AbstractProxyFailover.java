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
package com.wl4g.component.integration.sharding.failover;

import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats;

/**
 * {@link AbstractProxyFailover}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-18 v1.0.0
 * @since v1.0.0
 */
public abstract class AbstractProxyFailover<S extends NodeStats> implements ProxyFailover<S> {

    @Override
    public void start() {
        try {
            S result = inspect();
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
    }

    /**
     * Change read write splitting configuration.
     * 
     * @see https://shardingsphere.apache.org/document/current/cn/features/governance/management/registry-center/#metadataschemenamedatasources
     */
    protected void doChangeReadWriteSplititingConfiguration() {
        ProxyContext.getInstance();
    }

}
