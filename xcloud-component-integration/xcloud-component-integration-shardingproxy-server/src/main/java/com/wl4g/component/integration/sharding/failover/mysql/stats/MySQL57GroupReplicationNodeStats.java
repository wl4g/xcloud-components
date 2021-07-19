package com.wl4g.component.integration.sharding.failover.mysql.stats;

import java.util.List;

import com.wl4g.component.integration.sharding.failover.ProxyFailover.NodeStats;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MySQL57GroupReplicationNodeStats extends NodeStats {
    public static final String SQL_MGR_MEMBERS = "SELECT rgm.*, @@read_only AS READ_ONLY, @@super_read_only AS SUPER_READ_ONLY, (CASE @@super_read_only WHEN 0 THEN 'PRIMARY' ELSE 'SECONDARY' END) AS MEMBER_ROLE FROM `performance_schema`.`replication_group_members` rgm";

    private List<MGRNodeInfo> nodeInfos;

    @Getter
    @Setter
    public static class MGRNodeInfo {
        private String channelName;
        private String nodeId;
        private String nodeHost;
        private String nodePort;
        private String nodeState;
        private String nodeRole;
        private String readOnly;
        private String superReadOnly;
    }

}