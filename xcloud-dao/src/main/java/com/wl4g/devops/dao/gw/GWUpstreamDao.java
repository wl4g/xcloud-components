package com.wl4g.devops.dao.gw;

import org.apache.ibatis.annotations.Param;

import com.wl4g.components.core.bean.gw.GWUpstream;

import java.util.List;

public interface GWUpstreamDao {
    int deleteByPrimaryKey(Integer id);

    int insert(GWUpstream record);

    int insertSelective(GWUpstream record);

    GWUpstream selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(GWUpstream record);

    int updateByPrimaryKey(GWUpstream record);

    List<GWUpstream> list(@Param("organizationCodes") List<String> organizationCodes, @Param("name") String name);
}