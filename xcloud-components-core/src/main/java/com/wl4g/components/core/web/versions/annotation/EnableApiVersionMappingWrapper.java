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
package com.wl4g.components.core.web.versions.annotation;

import com.wl4g.components.core.web.versions.SimpleVersionComparator;

/**
 * {@link EnableApiVersionMappingWrapper}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-04
 * @sine v1.0
 * @see
 */
public class EnableApiVersionMappingWrapper {

	/**
	 * {@link EnableApiVersionMapping#sensitiveParams()}
	 */
	private final boolean sensitiveParams;

	/**
	 * {@link EnableApiVersionMapping#versionParams()}
	 */
	private final String[] versionParams;

	/**
	 * {@link EnableApiVersionMapping#groupParams()}
	 */
	private final String[] groupParams;

	/**
	 * {@link EnableApiVersionMapping#versionComparator()}
	 */
	private final SimpleVersionComparator versionComparator;

	public EnableApiVersionMappingWrapper(boolean sensitiveParams, String[] versionParams, String[] groupParams,
			SimpleVersionComparator versionComparator) {
		super();
		this.sensitiveParams = sensitiveParams;
		this.versionParams = versionParams;
		this.groupParams = groupParams;
		this.versionComparator = versionComparator;
	}

	public boolean isSensitiveParams() {
		return sensitiveParams;
	}

	public String[] getVersionParams() {
		return versionParams;
	}

	public String[] getGroupParams() {
		return groupParams;
	}

	public SimpleVersionComparator getVersionComparator() {
		return versionComparator;
	}

}
