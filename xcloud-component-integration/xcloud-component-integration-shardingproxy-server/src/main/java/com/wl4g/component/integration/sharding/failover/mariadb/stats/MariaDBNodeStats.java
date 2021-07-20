package com.wl4g.component.integration.sharding.failover.mariadb.stats;

import java.util.List;

import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats;

import lombok.Getter;
import lombok.Setter;

/**
 * {@link MariaDBNodeStats}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2021-07-18 v1.0.0
 * @since v1.0.0
 */
@Getter
@Setter
public class MariaDBNodeStats extends NodeStats {

    @Override
    public List<? extends NodeInfo> getPrimaryNodes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<? extends NodeInfo> getStandbyNodes() {
        throw new UnsupportedOperationException();
    }

}
