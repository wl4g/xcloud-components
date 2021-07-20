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
package com.wl4g.component.integration.sharding.failover.mysql;

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.integration.sharding.failover.mysql.stats.MySQL57GroupReplicationNodeStats.SQL_MGR_MEMBERS;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

import java.util.List;

import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

import com.wl4g.component.integration.sharding.failover.AbstractProxyFailover;
import com.wl4g.component.integration.sharding.failover.initializer.FailoverAbstractBootstrapInitializer;
import com.wl4g.component.integration.sharding.failover.jdbc.JdbcOperator;
import com.wl4g.component.integration.sharding.failover.mysql.stats.MySQL57GroupReplicationNodeStats;
import com.wl4g.component.integration.sharding.failover.mysql.stats.MySQL57GroupReplicationNodeStats.GroupReplicationNodeInfo;
import com.zaxxer.hikari.HikariDataSource;

/**
 * {@link MySQL57GroupReplicationProxyFailover}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-18 v1.0.0
 * @since v1.0.0
 */
public class MySQL57GroupReplicationProxyFailover extends AbstractProxyFailover<MySQL57GroupReplicationNodeStats> {

    public MySQL57GroupReplicationProxyFailover(FailoverAbstractBootstrapInitializer initializer,
            ShardingSphereMetaData metadata) {
        super(initializer, metadata);
    }

    @Override
    public MySQL57GroupReplicationNodeStats inspect() throws Exception {
        try (JdbcOperator operator = new JdbcOperator(getSelectedBackendNodeAdminDataSource())) {
            // Find group replication members information.
            List<GroupReplicationNodeInfo> nodes = operator.findAllBean(SQL_MGR_MEMBERS, new Object[0],
                    GroupReplicationNodeInfo.class);

            MySQL57GroupReplicationNodeStats stats = new MySQL57GroupReplicationNodeStats();
            stats.setNodes(safeList(nodes));
            // Single primary / multiple primary.
            stats.setPrimaryNodes(
                    stats.getNodes().stream().filter(n -> equalsIgnoreCase(n.getNodeRole(), "PRIMARY")).collect(toList()));
            // Salve nodes.
            stats.setStandbyNodes(
                    stats.getNodes().stream().filter(n -> equalsIgnoreCase(n.getNodeRole(), "STANDBY")).collect(toList()));
            return stats;
        }
    }

    @Override
    protected void decorateAdminBackendDataSource(String ruleDataSourceName, String ruleDataSourceJdbcHost,
            int ruleDataSourceJdbcPort, HikariDataSource adminDataSource) {
        adminDataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        adminDataSource.setJdbcUrl(format(MYSQL57_ADM_JDBC_URL_TPL, ruleDataSourceJdbcHost, ruleDataSourceJdbcPort));
        adminDataSource.setUsername("root");
        adminDataSource.setPassword("root");
    }

    private static final String MYSQL57_ADM_JDBC_URL_TPL = "jdbc:mysql://%s:%s/performance_schema?serverTimezone=UTC&useSSL=false&allowMultiQueries=true&characterEncoding=utf-8";
}
