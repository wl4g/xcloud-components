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

import java.util.List;
import java.util.Vector;

import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats;
import com.wl4g.component.integration.sharding.failover.mysql.MySQLGroupReplicationProxyFailover;
import com.wl4g.component.integration.sharding.failover.mysql.MySQLHAProxyFailover;
import com.wl4g.component.integration.sharding.failover.postgresql.PostgresqlProxyFailover;

/**
 * {@link ProxyFailoverManager}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-18 v1.0.0
 * @since v1.0.0
 */
public final class ProxyFailoverManager {

    private final List<ProxyFailover<? extends NodeStats>> failovers = new Vector<>();

    private ProxyFailoverManager() {
        failovers.add(new MySQLGroupReplicationProxyFailover());
        failovers.add(new MySQLHAProxyFailover());
        failovers.add(new PostgresqlProxyFailover());
    }

    public void startAll() {
        for (ProxyFailover<? extends NodeStats> failover : failovers) {
            failover.start();
        }
    }

    public void stopAll() {
        for (ProxyFailover<? extends NodeStats> failover : failovers) {
            failover.stop();
        }
    }

    public static ProxyFailoverManager getInstance() {
        return instance;
    }

    private static final ProxyFailoverManager instance = new ProxyFailoverManager();

}
