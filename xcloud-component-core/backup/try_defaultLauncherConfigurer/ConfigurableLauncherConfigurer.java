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
package com.wl4g.component.core.boot.listener;

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.lang.Assert2.isTrue;
import static com.wl4g.component.common.lang.ClassUtils2.isPresent;
import static com.wl4g.component.common.lang.Assert2.hasText;
import static java.lang.System.getenv;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang3.StringUtils.replaceAll;
import static org.apache.commons.lang3.StringUtils.replaceEach;
import static org.apache.commons.lang3.StringUtils.split;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Predicate;

import com.typesafe.config.Config;
import com.wl4g.component.common.typesafe.HoconConfigUtils;

/**
 * {@link ConfigurableLauncherConfigurer}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-06
 * @sine v1.0
 * @see
 */
public class ConfigurableLauncherConfigurer extends SpringLauncherConfigurer {

	public ConfigurableLauncherConfigurer() {
		super(DEFAULT_CONFIG_LOCATION);
	}

	/**
	 * Resolve generate spring boot default properties.
	 * 
	 * @return
	 */
	@Override
	public Properties resolveDefaultProperties() {
		Properties defaultProperties = new Properties();
		Launcher launcher = parse();
		for (LauncherConfiguration p : safeList(launcher.getConfigurations())) {
			if (matchsFilter(p.getFilter())) {
				defaultProperties.putAll(p.getProperties());
			}
			if (!p.isNext()) {
				break;
			}
		}
		if (launcher.isEnableLogVerbose()) {
			log.info("Resolved application preset config properties: {}", defaultProperties);
		}
		return defaultProperties;
	}

	private boolean matchsFilter(Filter filter) {
		if (filter.getOperator() == OperatorType.and) {
			for (String v : safeList(filter.getValue())) {
				if (!filter.getType().getPredicate().test(v)) {
					return false;
				}
			}
			return true;
		} else if (filter.getOperator() == OperatorType.or) {
			for (String v : safeList(filter.getValue())) {
				if (filter.getType().getPredicate().test(v)) {
					return true;
				}
			}
			return false;
		}
		return false;
	}

	private Launcher parse() {
		Config root = HoconConfigUtils.loadConfig(DEFAULT_CONFIG_LOCATION).resolve();

		Launcher launcher = new Launcher();
		launcher.setEnableLogVerbose(root.getBoolean("launcher.enable-log-verbose"));

		for (Config c : safeList(root.getConfigList("launcher.configurations"))) {
			LauncherConfiguration lc = new LauncherConfiguration();
			// Configuration filter
			Filter filter = new Filter();
			filter.setType(c.getEnum(FilterType.class, "filter.type"));
			filter.setOperator(c.getEnum(OperatorType.class, "filter.predicate"));
			filter.setValue(c.getStringList("filter.value"));
			lc.setFilter(filter);

			// Spring default properties
			Map<String, String> properties = c.getConfig("properties").entrySet().stream()
					.collect(toMap(e -> e.getKey(), e -> cleanNewLines(e.getValue().render())));
			lc.setProperties(properties);

			// Next
			lc.setNext(c.getBoolean("next"));
			launcher.getConfigurations().add(lc);
		}

		return launcher;
	}

	/**
	 * Clear newlines, tabs, and string type newlines.
	 * 
	 * @param value
	 * @return
	 */
	private String cleanNewLines(String value) {
		return replaceAll(replaceEach(value, CLEAR_NEWLINE_SEARCH, CLEAR_NEWLINE_REPLACEMENT), CLEAR_NEWLINE_REGEX, "");
	}

	class Launcher {
		private boolean enableLogVerbose = false;
		private List<LauncherConfiguration> configurations = new ArrayList<>();

		public boolean isEnableLogVerbose() {
			return enableLogVerbose;
		}

		public void setEnableLogVerbose(boolean enableLogVerbose) {
			this.enableLogVerbose = enableLogVerbose;
		}

		public List<LauncherConfiguration> getConfigurations() {
			return configurations;
		}

		public void setConfigurations(List<LauncherConfiguration> configurations) {
			this.configurations = configurations;
		}
	}

	class LauncherConfiguration {
		private Filter filter = new Filter();
		private Map<String, String> properties = new HashMap<>();
		private boolean next = false;

		public Filter getFilter() {
			return filter;
		}

		public void setFilter(Filter filter) {
			this.filter = filter;
		}

		public Map<String, String> getProperties() {
			return properties;
		}

		public void setProperties(Map<String, String> properties) {
			this.properties = properties;
		}

		public boolean isNext() {
			return next;
		}

		public void setNext(boolean next) {
			this.next = next;
		}
	}

	class Filter {
		private FilterType type = FilterType.hasClass;
		private OperatorType operator = OperatorType.and;
		private List<String> value;

		public FilterType getType() {
			return type;
		}

		public void setType(FilterType type) {
			this.type = type;
		}

		public OperatorType getOperator() {
			return operator;
		}

		public void setOperator(OperatorType operator) {
			this.operator = operator;
		}

		public List<String> getValue() {
			return value;
		}

		public void setValue(List<String> value) {
			this.value = value;
		}
	}

	enum FilterType {
		hasClass(input -> {
			hasText(input, "missingClass");
			return isPresent(input, null);
		}),

		missingClass(input -> {
			hasText(input, "missingClass");
			return !isPresent(input, null);
		}),

		eqEnv(input -> {
			isTrue(nonNull(input) && input.contains("="), "Unexpected expression of %s, for example: MY_KEY1=123", input);
			String[] keyValue = split(input, "=");
			return equalsIgnoreCase(getenv(trimToEmpty(keyValue[0])), trimToEmpty(keyValue[1]));
		}),

		always(input -> true);

		private final Predicate<String> predicate;

		private FilterType(Predicate<String> predicate) {
			this.predicate = predicate;
		}

		public Predicate<String> getPredicate() {
			return predicate;
		}
	}

	enum OperatorType {
		and, or
	}

	private static final String CLEAR_NEWLINE_REGEX = "\\s*|\t|\r|\n";
	private static final String[] CLEAR_NEWLINE_SEARCH = { "\\r", "\\n", "\r", "\n" };
	private static final String[] CLEAR_NEWLINE_REPLACEMENT = { "", "", "", "" };
	private static final String DEFAULT_CONFIG_LOCATION = "classpath*:/spring-config-strategy.conf";
}
