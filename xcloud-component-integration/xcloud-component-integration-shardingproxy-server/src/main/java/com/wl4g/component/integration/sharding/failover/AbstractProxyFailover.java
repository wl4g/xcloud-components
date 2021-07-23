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
import static com.wl4g.component.common.lang.Assert2.notEmpty;
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
import java.util.concurrent.ConcurrentHashMap;
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
import com.wl4g.component.common.task.RunnerProperties.StartupMode;
import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats;
import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats.NodeInfo;
import com.wl4g.component.integration.sharding.failover.initializer.FailoverAbstractBootstrapInitializer;
import com.wl4g.component.integration.sharding.failover.jdbc.JdbcOperator;
import com.wl4g.component.integration.sharding.util.HostUtil;
import com.wl4g.component.integration.sharding.util.JdbcUtil;
import com.wl4g.component.integration.sharding.util.JdbcUtil.JdbcInformation;
import com.zaxxer.hikari.HikariDataSource;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * {@link AbstractProxyFailover}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-18 v1.0.0
 * @since v1.0.0
 */
public abstract class AbstractProxyFailover<S extends NodeStats> extends GenericTaskRunner<RunnerProperties>
        implements ProxyFailover<S> {

    private final FailoverAbstractBootstrapInitializer initializer;
    private final ShardingSphereMetaData metadata;
    private final Map<String, HikariDataSource> cachingAdminDataSources = new ConcurrentHashMap<>(4);
    private FailoverSchemaConfiguration cachingFailoverSchemaConfig;

    public AbstractProxyFailover(FailoverAbstractBootstrapInitializer initializer, ShardingSphereMetaData metadata) {
        super(new RunnerProperties(StartupMode.NOSTARTUP, 1));
        this.initializer = notNullOf(initializer, "initializer");
        this.metadata = notNullOf(metadata, "metadata");
    }

    @Override
    protected void postStartupProperties() throws Exception {
        getWorker().scheduleWithRandomDelay(this, 1_000L, 6_000L, 10_000L, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run() {
        try {
            FailoverSchemaConfiguration oldFailoverConfig = loadFailoverSchemaConfiguration(true);

            // Other rule configurations do not need to be update.
            List<RuleConfiguration> newRuleConfigs = new ArrayList<>(oldFailoverConfig.getOtherRuleConfigs());

            boolean anyChanaged = false;
            for (ReadwriteSplittingRuleConfiguration oldRwRuleConfig : safeList(oldFailoverConfig.getReadwriteRuleConfigs())) {
                // New read-write-splitting dataSources.
                List<ReadwriteSplittingDataSourceRuleConfiguration> newRwDataSources = new ArrayList<>(4);

                for (ReadwriteSplittingDataSourceRuleConfiguration oldRwDataSource : safeList(oldRwRuleConfig.getDataSources())) {
                    // Obtain backend node admin dataSource.
                    DataSource adminDataSource = obtainSelectedBackendNodeAdminDataSource(oldRwDataSource.getName(),
                            oldRwDataSource.getReadDataSourceNames());

                    log.debug("Inspecting ... oldRwDataSourceConfig: {}, actual admin dataSource: {}",
                            () -> toJSONString(oldRwDataSource), () -> adminDataSource);

                    try (JdbcOperator operator = new JdbcOperator(adminDataSource);) {
                        // Inspect primary/standby latest information.
                        S result = inspecting(operator);
                        log.info("Inspected rwDataSourceName: {}, nodeInfo: {}", () -> oldRwDataSource.getName(),
                                () -> toJSONString(result));

                        // Selection new primary node.
                        notEmpty(result.getPrimaryNodes(), "Not found latest master node information");
                        NodeInfo newPrimaryNode = chooseNewPrimaryNode(result.getPrimaryNodes());

                        // TODO
                        // Transform db host/port to external(loadBalancer)
                        // host/port.
                        String newPrimaryDataSourceName = findMatchingNewPrimaryDataSourceName(
                                oldFailoverConfig.getAllDataSourceConfigs(), oldRwDataSource, newPrimaryNode);
                        String oldPrimaryDataSourceName = oldRwDataSource.getWriteDataSourceName();

                        // Check dataSource primary changed?
                        if (isChangedPrimaryNode(newPrimaryNode, newPrimaryDataSourceName, oldPrimaryDataSourceName)) {
                            // Gets changed new read dataSourceNames
                            List<String> newReadDataSourceNames = getChangedNewReadDataSourceNames(
                                    oldRwDataSource.getReadDataSourceNames(), newPrimaryDataSourceName);

                            // New read-write-splitting dataSource.
                            ReadwriteSplittingDataSourceRuleConfiguration newRwDataSource = new ReadwriteSplittingDataSourceRuleConfiguration(
                                    oldRwDataSource.getName(), oldRwDataSource.getAutoAwareDataSourceName(),
                                    newPrimaryDataSourceName, newReadDataSourceNames, oldRwDataSource.getLoadBalancerName());
                            newRwDataSources.add(newRwDataSource);
                            anyChanaged = true;
                        } else { // Not changed
                            newRwDataSources.add(oldRwDataSource);
                            log.debug(
                                    "Skiping change read-write-splitting, becuase primary dataSourceName it's up to date. {}, actualSchemaName: {}, schemaName: {}",
                                    oldPrimaryDataSourceName, oldRwDataSource.getName(), getSchemaName());
                        }
                    }
                }
                if (!newRwDataSources.isEmpty()) {
                    newRuleConfigs
                            .add(new ReadwriteSplittingRuleConfiguration(newRwDataSources, oldRwRuleConfig.getLoadBalancers()));
                }
            }

            if (anyChanaged && !newRuleConfigs.isEmpty()) {
                doChangeReadwriteSplittingRuleConfiguration(newRuleConfigs);
            }
        } catch (Exception e) {
            log.error("Failed to process backend nodes primary-standby failover.", e);
        }
    }

    protected DataSource obtainSelectedBackendNodeAdminDataSource(String haReadwriteDataSourceName,
            List<String> readDataSourceNames) throws SQLException {
        HikariDataSource adminDataSource = cachingAdminDataSources.get(haReadwriteDataSourceName);
        if (nonNull(adminDataSource)) {
            // Detecting & checking dataSource active?
            try {
                adminDataSource.getConnection().close();
                return adminDataSource;
            } catch (SQLException e) {
                adminDataSource.close();
                cachingAdminDataSources.remove(haReadwriteDataSourceName); // reset
                log.warn("Deaded caching dataSource: {}({}), reason: {}", adminDataSource, adminDataSource.getJdbcUrl(),
                        e.getMessage());
            }
        }
        log.info("Trying obtain next node admin dataSource ... - {}", haReadwriteDataSourceName);

        for (Entry<String, DataSource> ent : metadata.getResource().getDataSources().entrySet()) {
            String dsName = ent.getKey();
            // Skip data source names that do not belong to the data source
            // collection of the current read-write separation configuration.
            // (YAML tag: !READWRITE_SPLITTING)
            if (!safeList(readDataSourceNames).contains(dsName)) {
                continue;
            }

            DataSource ds = ent.getValue();
            try {
                // Find backend node business dataSource jdbcUrl.
                String jdbcUrl = null;
                if (ds instanceof HikariDataSource) {
                    jdbcUrl = ((HikariDataSource) ds).getJdbcUrl();
                } else {
                    throw new UnsupportedOperationException(format("No supported dataSource type. %s", ds.getClass()));
                }
                state(!isBlank(jdbcUrl), "Unable get backend node admin dataSource jdbcUrl.");

                // Build backend node admin connection.
                adminDataSource = new HikariDataSource();
                adminDataSource.setConnectionTimeout(6_000L);
                adminDataSource.setMaximumPoolSize(1);
                adminDataSource.setMinimumIdle(1);
                adminDataSource.setIdleTimeout(0L);
                adminDataSource.setMaxLifetime(180_000L);

                JdbcInformation info = JdbcUtil.resolve(jdbcUrl);
                decorateAdminBackendDataSource(dsName, info.getHost(), info.getPort(), adminDataSource);

                cachingAdminDataSources.put(haReadwriteDataSourceName, adminDataSource);
                return adminDataSource;
            } catch (Exception e) {
                log.warn("Cannot build backend connection of dataSourceName: {}", dsName);
            }
        }

        throw new SQLException(
                format("Failed to build backend connection. metadata dataSources: %s", metadata.getResource().getDataSources()));
    }

    protected abstract void decorateAdminBackendDataSource(String ruleDataSourceName, String ruleDataSourceJdbcHost,
            int ruldDataSourceJdbcPort, HikariDataSource adminDataSource);

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

    /**
     * Choosing new primary node {@link NodeInfo}
     * 
     * @param newPrimaryNodes
     * @return
     */
    protected NodeInfo chooseNewPrimaryNode(List<? extends NodeInfo> newPrimaryNodes) {
        return newPrimaryNodes.get(0); // By default
    }

    /**
     * Check whether the master node has been changed.
     * 
     * @param newPrimaryNode
     * @param newPrimaryDataSourceName
     * @param oldPrimaryDataSourceName
     * @see https://shardingsphere.apache.org/document/current/cn/features/governance/management/registry-center/#metadataschemenamedatasources
     * @return
     */
    protected boolean isChangedPrimaryNode(NodeInfo newPrimaryNode, String newPrimaryDataSourceName,
            String oldPrimaryDataSourceName) {
        return !StringUtils2.equals(oldPrimaryDataSourceName, newPrimaryDataSourceName);
    }

    /**
     * Gets changed new read dataSource names.
     * 
     * @param oldReadDataSourceNames
     * @param newPrimaryDataSourceName
     * @return
     */
    private List<String> getChangedNewReadDataSourceNames(List<String> oldReadDataSourceNames, String newPrimaryDataSourceName) {
        List<String> newReadDataSourceNames = new ArrayList<>(oldReadDataSourceNames);
        newReadDataSourceNames.remove(newPrimaryDataSourceName);
        return newReadDataSourceNames;
    }

    private synchronized FailoverSchemaConfiguration loadFailoverSchemaConfiguration(boolean useCache) {
        if (useCache && nonNull(cachingFailoverSchemaConfig)) {
            return cachingFailoverSchemaConfig;
        }

        Map<String, DataSourceConfiguration> allDataSourceConfigs = initializer.loadDataSourceConfigs(getSchemaName());
        Collection<RuleConfiguration> allRuleConfigs = initializer.loadRuleConfigs(getSchemaName());

        List<ReadwriteSplittingRuleConfiguration> readwriteRuleConfigs = new ArrayList<>(2);
        List<RuleConfiguration> otherRuleConfigs = new ArrayList<>(2);
        for (RuleConfiguration ruleConfig : safeList(allRuleConfigs)) {
            if (ruleConfig instanceof ReadwriteSplittingRuleConfiguration) {
                readwriteRuleConfigs.add((ReadwriteSplittingRuleConfiguration) ruleConfig);
            } else {
                otherRuleConfigs.add(ruleConfig);
            }
        }

        return new FailoverSchemaConfiguration(allDataSourceConfigs, allRuleConfigs, readwriteRuleConfigs, otherRuleConfigs);
    }

    /**
     * Transform matching configuration dataSource name by host and port.
     * 
     * @param allDataSourceConfigs
     * @param oldRwDataSource
     * @param node
     * @return
     */
    private String findMatchingNewPrimaryDataSourceName(Map<String, DataSourceConfiguration> allDataSourceConfigs,
            ReadwriteSplittingDataSourceRuleConfiguration oldRwDataSource, NodeInfo node) {

        List<String> oldAllRwDataSourceNames = new ArrayList<>(oldRwDataSource.getReadDataSourceNames());
        oldAllRwDataSourceNames.add(oldRwDataSource.getWriteDataSourceName());

        for (Entry<String, DataSourceConfiguration> ent : safeMap(allDataSourceConfigs).entrySet()) {
            String defineDataSourceName = ent.getKey();
            String jdbcUrl = valueOf(ent.getValue().getProps().get("jdbcUrl"));

            if (oldAllRwDataSourceNames.contains(defineDataSourceName)) {
                JdbcInformation info = JdbcUtil.resolve(jdbcUrl);
                if (info.getPort() == node.getPort() && HostUtil.isSameHost(info.getHost(), node.getHost())) {
                    return ent.getKey(); // Define dataSource name.
                }
            }
        }

        throw new IllegalStateException(format("No found dataSource name by host: %s, port: %s", node.getHost(), node.getPort()));
    }

    /**
     * Do changing readWriteSplitting rule configuration.
     * 
     * @param newReadWriteSplittingRuleConfigs
     */
    private void doChangeReadwriteSplittingRuleConfiguration(List<RuleConfiguration> newReadWriteSplittingRuleConfigs) {
        log.info("Do changed new read-write-splitting rule configuration ... - {}", newReadWriteSplittingRuleConfigs);
        initializer.updateSchemaRuleConfiguration(getSchemaName(), newReadWriteSplittingRuleConfigs);
    }

    @Getter
    @AllArgsConstructor
    static class FailoverSchemaConfiguration {
        private Map<String, DataSourceConfiguration> allDataSourceConfigs;
        private Collection<RuleConfiguration> allRuleConfigs;
        private List<ReadwriteSplittingRuleConfiguration> readwriteRuleConfigs;
        private List<RuleConfiguration> otherRuleConfigs;
    }

}
