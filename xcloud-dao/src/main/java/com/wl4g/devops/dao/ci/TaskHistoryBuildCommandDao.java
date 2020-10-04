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
package com.wl4g.devops.dao.ci;

import java.util.List;

import com.wl4g.components.core.bean.ci.TaskBuildCommand;

public interface TaskHistoryBuildCommandDao {
	int deleteByPrimaryKey(Long id);

	int insert(TaskBuildCommand record);

	int insertSelective(TaskBuildCommand record);

	TaskBuildCommand selectByPrimaryKey(Long id);

	List<TaskBuildCommand> selectByTaskHisId(Long taskId);

	int updateByPrimaryKeySelective(TaskBuildCommand record);

	int updateByPrimaryKey(TaskBuildCommand record);
}