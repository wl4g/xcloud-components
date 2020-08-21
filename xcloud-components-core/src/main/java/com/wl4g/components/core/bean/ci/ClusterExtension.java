package com.wl4g.components.core.bean.ci;

import com.wl4g.components.core.bean.BaseBean;

public class ClusterExtension extends BaseBean {

    private static final long serialVersionUID = 6815608076300843748L;

    private Integer clusterId;

    private String defaultEnv;

    private String defaultBranch;

    //other
    private String clusterName;

    public Integer getClusterId() {
        return clusterId;
    }

    public void setClusterId(Integer clusterId) {
        this.clusterId = clusterId;
    }

    public String getDefaultEnv() {
        return defaultEnv;
    }

    public void setDefaultEnv(String defaultEnv) {
        this.defaultEnv = defaultEnv == null ? null : defaultEnv.trim();
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public void setDefaultBranch(String defaultBranch) {
        this.defaultBranch = defaultBranch == null ? null : defaultBranch.trim();
    }

    public String getClusterName() {
        return clusterName;
    }

    public void setClusterName(String clusterName) {
        this.clusterName = clusterName;
    }
}