/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.wl4g.component.integration.sharding.algorithm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.shardingsphere.sharding.api.sharding.ShardingAutoTableAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.RangeShardingValue;
import org.apache.shardingsphere.sharding.api.sharding.standard.StandardShardingAlgorithm;

import com.google.common.collect.Range;

import lombok.Getter;
import lombok.Setter;

/**
 * Abstract range sharding algorithm.
 */
public abstract class AbstractRangeShardingAlgorithm2 implements StandardShardingAlgorithm<Long>, ShardingAutoTableAlgorithm {

    private volatile Map<Integer, Range<Long>> partitionRange;

    @Getter
    @Setter
    private Properties props = new Properties();

    @Override
    public final void init() {
        partitionRange = calculatePartitionRange(props);
    }

    protected abstract Map<Integer, Range<Long>> calculatePartitionRange(Properties props);

    @Override
    public final String doSharding(final Collection<String> availableTargetNames,
            final PreciseShardingValue<Long> shardingValue) {
        return availableTargetNames.stream().filter(each -> each.endsWith(String.valueOf(getPartition(shardingValue.getValue()))))
                .findFirst().orElse(null);
    }

    @Override
    public final Collection<String> doSharding(final Collection<String> availableTargetNames,
            final RangeShardingValue<Long> shardingValue) {
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
        int firstPartition = getFirstPartition(shardingValue.getValueRange());
        int lastPartition = getLastPartition(shardingValue.getValueRange());
        for (int partition = firstPartition; partition <= lastPartition; partition++) {
            for (String each : availableTargetNames) {
                if (each.endsWith(String.valueOf(partition))) {
                    result.add(each);
                }
            }
        }
        return result;
    }

    private int getFirstPartition(final Range<Long> valueRange) {
        return valueRange.hasLowerBound() ? getPartition(valueRange.lowerEndpoint()) : 0;
    }

    private int getLastPartition(final Range<Long> valueRange) {
        return valueRange.hasUpperBound() ? getPartition(valueRange.upperEndpoint()) : partitionRange.size() - 1;
    }

    private Integer getPartition(final Number value) {
        //
        // FIXED: type compatible.
        //
        Long longValue = (value instanceof Long) ? (Long) value : value.longValue();

        for (Entry<Integer, Range<Long>> entry : partitionRange.entrySet()) {
            if (entry.getValue().contains(longValue)) {
                return entry.getKey();
            }
        }
        throw new UnsupportedOperationException("");
    }

    @Override
    public final int getAutoTablesAmount() {
        return partitionRange.size();
    }
}
