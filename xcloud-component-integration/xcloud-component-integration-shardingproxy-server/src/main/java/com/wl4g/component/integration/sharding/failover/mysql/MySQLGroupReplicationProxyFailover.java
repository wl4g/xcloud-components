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

import java.sql.Connection;

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MariaDBDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OracleDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import com.wl4g.component.integration.sharding.failover.jdbc.JdbcOperator;
import com.wl4g.component.integration.sharding.failover.mysql.stats.MySQLGroupReplicationNodeStats;
import com.wl4g.component.integration.sharding.failover.mysql.stats.MySQLGroupReplicationNodeStats.MGRNodeInfo;

/**
 * {@link MySQLGroupReplicationProxyFailover}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-18 v1.0.0
 * @since v1.0.0
 */
public class MySQLGroupReplicationProxyFailover extends MySQLProxyFailover<MySQLGroupReplicationNodeStats> {

    @Override
    public MySQLGroupReplicationNodeStats inspect() throws Exception {
        ProxyContext proxy = ProxyContext.getInstance();
        for (String schema : proxy.getAllSchemaNames()) {
            ShardingSphereMetaData meta = proxy.getMetaData(schema);
            DatabaseType databaseType = meta.getResource().getDatabaseType();
            if (databaseType instanceof MySQLDatabaseType) {

            } else if (databaseType instanceof PostgreSQLDatabaseType) {

            } else if (databaseType instanceof OracleDatabaseType) {

            } else if (databaseType instanceof H2DatabaseType) {

            } else if (databaseType instanceof MariaDBDatabaseType) {

            }
            System.out.println(meta);
        }
        Connection connection = proxy.getBackendDataSource().getConnection("userdb", "userdb_g1db0");
        try (JdbcOperator operator = new JdbcOperator(connection)) {
            return new MySQLGroupReplicationNodeStats(
                    operator.findAllBean(MySQLGroupReplicationNodeStats.SQL_MGR_MEMBERS, new Object[0], MGRNodeInfo.class));
        }
    }

}
