package com.wl4g.component.integration.sharding.failover.mysql.stats;

import java.util.List;

import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MySQL57GroupReplicationNodeStats extends NodeStats {

    private List<GroupReplicationNodeInfo> nodes;

    private List<GroupReplicationNodeInfo> primaryNodes;

    private List<GroupReplicationNodeInfo> standbyNodes;

    @Getter
    @Setter
    public static class GroupReplicationNodeInfo extends NodeInfo {
        private String channelName;
        private String nodeId;
        private String nodeHost;
        private Integer nodePort;
        private String nodeState;
        private String nodeRole;
        private String readOnly;
        private String superReadOnly;

        @Override
        public String getHost() {
            return nodeHost;
        }

        @Override
        public int getPort() {
            return nodePort;
        }

    }

}