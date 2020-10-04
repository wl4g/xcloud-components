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
package com.wl4g.components.core.bean.ci;

import java.io.Serializable;

import com.wl4g.components.core.bean.BaseBean;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskHistory extends BaseBean implements Serializable {
	private static final long serialVersionUID = 381411777614066880L;

	private Integer type;
	private Long projectId;
	private Integer status;
	private String branchName;
	private String shaGit;
	private String shaLocal;
	private Long refId;
	private String buildCommand;
	private String preCommand;
	private String postCommand;
	private String providerKind;
	private String branchType;
	private String result;
	private String projectName;
	private String groupName;
	private Long contactGroupId;
	private String trackId;
	private String trackType;
	private Long costTime;
	private String envType;
	private String createByName;
	private String annex;
	private String parentAppHome;
	/** 构建的文件/目录路径（maven项目的target目录，vue项目的dist目录） */
	private String assetsPath;

}