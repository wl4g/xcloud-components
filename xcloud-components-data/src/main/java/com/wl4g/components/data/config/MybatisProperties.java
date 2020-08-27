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
package com.wl4g.components.data.config;

import static com.wl4g.components.common.serialize.JacksonUtils.toJSONString;
import static java.util.Arrays.asList;

import java.util.List;

/**
 * {@link MybatisProperties}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-08-05
 * @since
 */
public class MybatisProperties {

	private List<String> typeAliasPackage = asList("com.wl4g.components.data.bean.*");
	private String configLocation = "mybatis/mybatis-config.xml";
	private List<String> mapperLocations = asList("classpath:mybatis/**/*Mapper.xml");

	public List<String> getTypeAliasPackage() {
		return typeAliasPackage;
	}

	public void setTypeAliasPackage(List<String> typeAliasesPackage) {
		this.typeAliasPackage = typeAliasesPackage;
	}

	public String getConfigLocation() {
		return configLocation;
	}

	public void setConfigLocation(String configLocation) {
		this.configLocation = configLocation;
	}

	public List<String> getMapperLocations() {
		return mapperLocations;
	}

	public void setMapperLocations(List<String> mapperLocations) {
		this.mapperLocations = mapperLocations;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().concat(" - ").concat(toJSONString(this));
	}

}
