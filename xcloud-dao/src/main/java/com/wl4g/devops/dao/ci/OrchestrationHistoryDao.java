package com.wl4g.devops.dao.ci;

import com.wl4g.components.core.bean.ci.OrchestrationHistory;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrchestrationHistoryDao {
    int deleteByPrimaryKey(Integer id);

    int insert(OrchestrationHistory record);

    int insertSelective(OrchestrationHistory record);

    OrchestrationHistory selectByPrimaryKey(Integer id);

    OrchestrationHistory selectByRunId(String runId);

    int updateByPrimaryKeySelective(OrchestrationHistory record);

    int updateByPrimaryKey(OrchestrationHistory record);

    List<OrchestrationHistory> list(@Param("organizationCodes")List<String> organizationCodes, @Param("runId") String runId);
}