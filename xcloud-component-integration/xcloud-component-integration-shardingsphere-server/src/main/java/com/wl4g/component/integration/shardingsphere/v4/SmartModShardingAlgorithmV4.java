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
package com.wl4g.component.integration.shardingsphere.v4;

import static com.wl4g.component.common.collection.CollectionUtils2.safeArrayToList;
import static com.wl4g.component.common.lang.Assert2.isTrue;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static java.util.Collections.synchronizedMap;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.shardingsphere.api.sharding.standard.PreciseShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.PreciseShardingValue;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.standard.RangeShardingValue;

import lombok.Getter;

/**
 * {@link SmartModShardingAlgorithmV4}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-04-19
 * @sine v1.0
 * @see
 */
public class SmartModShardingAlgorithmV4
		implements PreciseShardingAlgorithm<Comparable<?>>, RangeShardingAlgorithm<Comparable<?>> {

	private static final String SHARDING_MOD_ASSIGN_EXPRESSION_KEY = "sharding-mod-assign-expression";

	private Properties props = new Properties();
	private ShardingModAssignExpression shardingModAssignExpression;
	private int shardingAllTableCount;

	@Override
	public Collection<String> doSharding(Collection<String> availableTargetNames,
			RangeShardingValue<Comparable<?>> shardingValue) {
		return isContainAllTargets(shardingValue) ? availableTargetNames
				: getAvailableTargetNames(availableTargetNames, shardingValue);
	}

	@Override
	public String doSharding(Collection<String> availableTargetNames, PreciseShardingValue<Comparable<?>> shardingValue) {
		for (String targetName : availableTargetNames) {
			List<Long> mods = shardingModAssignExpression.getShardingAssign().get(targetName);
			if (nonNull(mods) && mods.contains(getLongValue(shardingValue.getValue()) % shardingAllTableCount)) {
				return targetName;
			}
		}
		return null;
	}

	private boolean isContainAllTargets(final RangeShardingValue<Comparable<?>> shardingValue) {
		return !shardingValue.getValueRange().hasUpperBound()
				|| shardingValue.getValueRange().hasLowerBound() && getLongValue(shardingValue.getValueRange().upperEndpoint())
						- getLongValue(shardingValue.getValueRange().lowerEndpoint()) >= shardingAllTableCount - 1;
	}

	private Collection<String> getAvailableTargetNames(final Collection<String> availableTargetNames,
			final RangeShardingValue<Comparable<?>> shardingValue) {
		Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());
		for (long i = getLongValue(shardingValue.getValueRange().lowerEndpoint()); i <= getLongValue(
				shardingValue.getValueRange().upperEndpoint()); i++) {
			for (String targetName : availableTargetNames) {
				List<Long> mods = shardingModAssignExpression.getShardingAssign().get(targetName);
				if (nonNull(mods) && mods.contains(i % shardingAllTableCount)) {
					result.add(targetName);
				}
			}
		}
		return result;
	}

	private long getLongValue(final Comparable<?> value) {
		return Long.parseLong(value.toString());
	}

	@Getter
	static final class ShardingModAssignExpression {
		private final Map<String, List<Long>> shardingAssign = synchronizedMap(new HashMap<>(4));

		public static ShardingModAssignExpression parse(String expression) {
			notNullOf(expression, "Sharding mod assign expression cannot be null.");
			isTrue(expression.contains("->"), ERR_INVALID_EXPRESSION_MSG);

			ShardingModAssignExpression parsed = new ShardingModAssignExpression();
			for (String parts : safeArrayToList(split(expression, "|"))) {
				String[] dbAndMods = split(trimToEmpty(parts), "->"); // g0db0->0,1,2,3
				isTrue(nonNull(dbAndMods) && dbAndMods.length == 2, ERR_INVALID_EXPRESSION_MSG);

				List<Long> mods = safeArrayToList(split(dbAndMods[1], ",")).stream().map(m -> Long.parseLong(trimToEmpty(m)))
						.collect(toList());
				isTrue(!mods.isEmpty(), ERR_INVALID_EXPRESSION_MSG);

				parsed.shardingAssign.put(trimToEmpty(dbAndMods[0]), mods);
			}
			return parsed;
		}

		public static final String ERR_INVALID_EXPRESSION_MSG = "Sharding mod assign expression invalid. for example: 'g0db0->0,1,2,3|g0db1->4,5,6|g0db2->7,8,9'";
	}

}
