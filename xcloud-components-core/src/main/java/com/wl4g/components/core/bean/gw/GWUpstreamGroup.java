package com.wl4g.components.core.bean.gw;

import java.util.List;

import com.wl4g.components.core.bean.BaseBean;

public class GWUpstreamGroup extends BaseBean {
    private static final long serialVersionUID = -3298424126317938674L;

    private String name;

    private List<GWUpstreamGroupRef> gwUpstreamGroupRefs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name == null ? null : name.trim();
    }

    public List<GWUpstreamGroupRef> getGwUpstreamGroupRefs() {
        return gwUpstreamGroupRefs;
    }

    public void setGwUpstreamGroupRefs(List<GWUpstreamGroupRef> gwUpstreamGroupRefs) {
        this.gwUpstreamGroupRefs = gwUpstreamGroupRefs;
    }




}