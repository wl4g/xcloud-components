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
import static com.wl4g.component.common.lang.Assert2.state;
import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;

import com.wl4g.component.common.lang.StringUtils2;
import com.wl4g.component.common.task.GenericTaskRunner;
import com.wl4g.component.common.task.RunnerProperties;
import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats;
import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats.NodeInfo;
import com.wl4g.component.integration.sharding.failover.initializer.FailoverAbstractBootstrapInitializer;
import com.wl4g.component.integration.sharding.util.HostUtil;
import com.wl4g.component.integration.sharding.util.JdbcUtil;
import com.wl4g.component.integration.sharding.util.JdbcUtil.JdbcInformation;
import com.zaxxer.hikari.HikariDataSource;

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
    private DataSource cachingAdminDataSource;

    public AbstractProxyFailover(FailoverAbstractBootstrapInitializer initializer, ShardingSphereMetaData metadata) {
        super(new RunnerProperties(true).withConcurrency(1));
        this.initializer = notNullOf(initializer, "initializer");
        this.metadata = notNullOf(metadata, "metadata");
    }

    @Override
    protected void postStartupProperties() throws Exception {
        getWorker().scheduleWithRandomDelay(this, 6_000L, 6_000L, 10_000L, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            S result = inspect();
            log.debug("Inspect result information: {}", () -> toJSONString(result));

            // TODO
            // Selection new primary node.
            NodeInfo newPrimaryNode = result.getPrimaryNodes().get(0);

            // TODO
            // Transform database host/port to external(loadBalancer) host/port.

            changeReadwriteSplititingConfiguration(
                    findMatchingDataSourceName(newPrimaryNode.getHost(), newPrimaryNode.getPort()));

        } catch (Exception e) {
            log.error("Failed to failover inspecting.", e);
        }
    }

    protected DataSource getSelectedBackendNodeAdminDataSource() throws SQLException {
        if (nonNull(cachingAdminDataSource)) {
            // Detecting & checking dataSource active?
            try {
                cachingAdminDataSource.getConnection().close();
                return cachingAdminDataSource;
            } catch (SQLException e) {
                log.warn("Deaded caching dataSource: {}, reason: ", cachingAdminDataSource, e.getMessage());
                cachingAdminDataSource = null; // reset
            }
        }

        log.info("Trying build next admin dataSource ...");

        for (Entry<String, DataSource> ent : metadata.getResource().getDataSources().entrySet()) {
            String dataSourceName = ent.getKey();
            DataSource dataSource = ent.getValue();
            try {
                // Find backend node business dataSource jdbcUrl.
                String jdbcUrl = null;
                if (dataSource instanceof HikariDataSource) {
                    jdbcUrl = ((HikariDataSource) dataSource).getJdbcUrl();
                } else {
                    throw new UnsupportedOperationException(format("No supported dataSource type. %s", dataSource.getClass()));
                }
                state(!isBlank(jdbcUrl), "Unable get backend node admin dataSource jdbcUrl.");

                // Build backend node admin connection.
                HikariDataSource adminDataSource = new HikariDataSource();
                adminDataSource.setConnectionTimeout(10_000L);
                adminDataSource.setMaximumPoolSize(2);
                adminDataSource.setMinimumIdle(1);
                adminDataSource.setIdleTimeout(6_000L);
                adminDataSource.setMaxLifetime(180_000L);

                JdbcInformation info = JdbcUtil.resolve(jdbcUrl);
                decorateAdminBackendDataSource(dataSourceName, info.getHost(), info.getPort(), adminDataSource);
                return (cachingAdminDataSource = adminDataSource);
            } catch (Exception e) {
                log.warn("Cannot build backend connection of dataSourceName: {}", dataSourceName);
            }
        }

        throw new SQLException(
                format("Failed to build backend connection. metadata dataSources: %s", metadata.getResource().getDataSources()));
    }

    protected abstract void decorateAdminBackendDataSource(String ruleDataSourceName, String ruleDataSourceJdbcHost,
            int ruldDataSourceJdbcPort, HikariDataSource adminDataSource);

    /**
     * Find match configuration dataSource name by host and port.
     * 
     * @param host
     * @param port
     * @return
     */
    protected String findMatchingDataSourceName(String host, int port) {
        Map<String, DataSourceConfiguration> dataSourceConfigs = initializer.loadDataSourceConfigs(getSchemaName());
        for (Entry<String, DataSourceConfiguration> ent : safeMap(dataSourceConfigs).entrySet()) {
            String jdbcUrl = valueOf(ent.getValue().getProps().get("jdbcUrl"));

            JdbcInformation info = JdbcUtil.resolve(jdbcUrl);
            if (info.getPort() == port && HostUtil.isSameHost(info.getHost(), host)) {
                return ent.getKey(); // Define dataSource name.
            }
        }
        throw new IllegalStateException(format("No found dataSource name by host: %s, port: %s", host, port));
    }

    /**
     * Changing read write splitting configuration.(if necessary)
     * 
     * @param newPrimaryDataSourceName
     * @see https://shardingsphere.apache.org/document/current/cn/features/governance/management/registry-center/#metadataschemenamedatasources
     */
    protected void changeReadwriteSplititingConfiguration(String newPrimaryDataSourceName) {
        List<ReadwriteSplittingRuleConfiguration> newReadwriteSplittingRuleConfigs = new ArrayList<>(4);

        Collection<RuleConfiguration> ruleConfigs = initializer.loadRuleConfigs(getSchemaName());
        for (RuleConfiguration ruleConfig : safeList(ruleConfigs)) {
            if (ruleConfig instanceof ReadwriteSplittingRuleConfiguration) {
                ReadwriteSplittingRuleConfiguration rwRuleConfig = (ReadwriteSplittingRuleConfiguration) ruleConfig;

                // New build read-write-splitting dataSources.
                List<ReadwriteSplittingDataSourceRuleConfiguration> newRwDataSources = new ArrayList<>(4);
                newReadwriteSplittingRuleConfigs
                        .add(new ReadwriteSplittingRuleConfiguration(newRwDataSources, rwRuleConfig.getLoadBalancers()));

                for (ReadwriteSplittingDataSourceRuleConfiguration rwDataSource : safeList(rwRuleConfig.getDataSources())) {
                    // Check dataNodes primary changed?
                    String oldPrimaryDataSourceName = rwDataSource.getWriteDataSourceName();
                    if (!StringUtils2.equals(oldPrimaryDataSourceName, newPrimaryDataSourceName)) {
                        log.info(
                                "Changing readWriteSplitting old primaryDataSourceName: {} to new primaryDataSourceName: {}, actualSchemaName: {}, schemaName: {}",
                                oldPrimaryDataSourceName, newPrimaryDataSourceName, rwDataSource.getName(), getSchemaName());

                        // New build read-write-splitting dataSource.
                        ReadwriteSplittingDataSourceRuleConfiguration newRwDataSource = new ReadwriteSplittingDataSourceRuleConfiguration(
                                rwDataSource.getName(), rwDataSource.getAutoAwareDataSourceName(), newPrimaryDataSourceName, null,
                                rwDataSource.getLoadBalancerName());
                        newRwDataSources.add(newRwDataSource);
                    } else {
                        log.info(
                                "Skiping change readWriteSplitting, becuase primaryDataSourceName it's up to date. {}, actualSchemaName: {}, schemaName: {}",
                                oldPrimaryDataSourceName, rwDataSource.getName(), getSchemaName());
                    }
                }
            }
        }

        if (!newReadwriteSplittingRuleConfigs.isEmpty()) {
            doChangeReadwriteSplittingRuleConfiguration(newReadwriteSplittingRuleConfigs);
        }
    }

    /**
     * Do changing readWriteSplitting rule configuration.
     * 
     * @param newReadWriteSplittingRuleConfigs
     */
    protected void doChangeReadwriteSplittingRuleConfiguration(
            List<ReadwriteSplittingRuleConfiguration> newReadWriteSplittingRuleConfigs) {
        initializer.updateSchemaRuleConfiguration(getSchemaName(), newReadWriteSplittingRuleConfigs);
    }

    /**
     * Gets the schema name of the target to be processed by the current
     * failover.
     * 
     * Notes: a failover instance is responsible for monitoring a schema.
     * 
     * @return
     */
    protected String getSchemaName() {
        return metadata.getName();
    }

}
