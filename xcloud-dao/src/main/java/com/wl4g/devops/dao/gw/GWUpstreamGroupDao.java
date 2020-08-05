package com.wl4g.devops.dao.gw;

import org.apache.ibatis.annotations.Param;

import com.wl4g.components.core.bean.gw.GWCluster;
import com.wl4g.components.core.bean.gw.GWUpstreamGroup;

import java.util.List;

public interface GWUpstreamGroupDao {
    int deleteByPrimaryKey(Integer id);

    int insert(GWUpstreamGroup record);

    int insertSelective(GWUpstreamGroup record);

    GWUpstreamGroup selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(GWUpstreamGroup record);

    int updateByPrimaryKey(GWUpstreamGroup record);

    List<GWUpstreamGroup> list(@Param("organizationCodes") List<String> organizationCodes, @Param("name") String name);
}