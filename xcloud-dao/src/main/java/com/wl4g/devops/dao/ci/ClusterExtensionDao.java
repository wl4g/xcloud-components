package com.wl4g.devops.dao.ci;

import com.wl4g.components.core.bean.ci.ClusterExtension;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClusterExtensionDao {
    int deleteByPrimaryKey(Integer id);

    int insert(ClusterExtension record);

    int insertSelective(ClusterExtension record);

    ClusterExtension selectByPrimaryKey(Integer id);

    ClusterExtension selectByClusterId(Integer clusterId);

    ClusterExtension selectByClusterName(String clusterName);

    int updateByPrimaryKeySelective(ClusterExtension record);

    int updateByPrimaryKey(ClusterExtension record);

    List<ClusterExtension> list(@Param("clusterName") String clusterName);
}