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

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.lang.String.format;
import static java.lang.String.valueOf;

import java.net.URI;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;

import com.wl4g.component.common.lang.StringUtils2;
import com.wl4g.component.common.task.GenericTaskRunner;
import com.wl4g.component.common.task.RunnerProperties;
import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats;
import com.wl4g.component.integration.sharding.failover.initializer.FailoverAbstractBootstrapInitializer;

/**
 * {@link AbstractProxyFailover}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-18 v1.0.0
 * @since v1.0.0
 */
public abstract class AbstractProxyFailover<S extends NodeStats> extends GenericTaskRunner<RunnerProperties>
        implements ProxyFailover<S> {
    protected final FailoverAbstractBootstrapInitializer initializer;
    protected final ShardingSphereMetaData metadata;

    public AbstractProxyFailover(FailoverAbstractBootstrapInitializer initializer, ShardingSphereMetaData metadata) {
        super(new RunnerProperties(true).withConcurrency(1));
        this.initializer = notNullOf(initializer, "initializer");
        this.metadata = notNullOf(metadata, "metadata");
    }

    @Override
    protected void postStartupProperties() throws Exception {
        getWorker().scheduleWithRandomDelay(this, 6_000L, 6_000L, 20_000L, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run() {
        try {
            S result = inspect();
            System.out.println(result);
        } catch (Exception e) {
            log.error("Failed to failover inspecting.", e);
        }
    }

    protected Connection getBackendSelectedNodeConnection() throws SQLException {
        for (Entry<String, DataSource> ent : metadata.getResource().getDataSources().entrySet()) {
            String dataSourceName = ent.getKey();
            try {
                return ProxyContext.getInstance().getBackendDataSource().getConnection(metadata.getName(), dataSourceName);
            } catch (SQLException e) {
                log.warn("Unable get backend connection of dataSourceName: {}", dataSourceName);
            }
        }
        throw new SQLException(format("Unable get backend connection"));
    }

    /**
     * Changing read write splitting configuration.(if necessary)
     * 
     * @see https://shardingsphere.apache.org/document/current/cn/features/governance/management/registry-center/#metadataschemenamedatasources
     */
    protected void changeReadWriteSplititingConfiguration(String newPrimaryDataSourceName) {
        // Check dataNodes primary changed?
        Collection<RuleConfiguration> ruleConfigs = initializer.loadRuleConfigs(metadata.getName());
        for (RuleConfiguration ruleConfig : safeList(ruleConfigs)) {
            if (ruleConfig instanceof ReadwriteSplittingRuleConfiguration) {
                ReadwriteSplittingRuleConfiguration rwRuleConfig = (ReadwriteSplittingRuleConfiguration) ruleConfig;
                for (ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig : safeList(
                        rwRuleConfig.getDataSources())) {
                    String oldPrimaryDataSourceName = dataSourceRuleConfig.getWriteDataSourceName();
                    if (!StringUtils2.equals(oldPrimaryDataSourceName, newPrimaryDataSourceName)) {
                        log.info(
                                "Changing readWriteSplitting old primaryDataSourceName: {} to new primaryDataSourceName: {}, actualSchemaName: {}, schemaName: {}",
                                oldPrimaryDataSourceName, newPrimaryDataSourceName, dataSourceRuleConfig.getName(),
                                metadata.getName());
                        doChangeReadWriteSplittingRuleConfiguration();
                    } else {
                        log.info(
                                "Skip change readWriteSplitting, becuase primaryDataSourceName it's up to date. {}, actualSchemaName: {}, schemaName: {}",
                                oldPrimaryDataSourceName, dataSourceRuleConfig.getName(), metadata.getName());
                    }
                }
            }
        }
    }

    protected String findMatchingDataSourceName(String host, int port) {
        Map<String, DataSourceConfiguration> dataSourceConfigs = initializer.loadDataSourceConfigs(metadata.getName());
        for (Entry<String, DataSourceConfiguration> ent : safeMap(dataSourceConfigs).entrySet()) {
            String url = valueOf(ent.getValue().getProps().get("url"));
            // e.g:jdbc:mysql://127.0.0.1:3306/userdb_g1db0?serverTimezone=UTC&useSSL=false&allowMultiQueries=true&characterEncoding=utf-8
            URI uri = URI.create(url);
            if (StringUtils2.equals(uri.getHost(), host) && uri.getPort() == port) {
                return ent.getKey(); // Define dataSource name.
            }
        }
        throw new IllegalStateException(format("Not found dataSource name by host: %s, port: %s", host, port));
    }

    protected void doChangeReadWriteSplittingRuleConfiguration() {
        initializer.updateSchemaRuleConfiguration(null);
    }

}
