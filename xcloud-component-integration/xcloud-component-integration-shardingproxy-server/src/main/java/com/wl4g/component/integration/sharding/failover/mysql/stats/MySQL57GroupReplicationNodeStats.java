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
    public static final String SQL_MGR_MEMBERS = "SELECT rgm.CHANNEL_NAME AS channelName,rgm.MEMBER_ID AS nodeId,rgm.MEMBER_HOST AS nodeHost,rgm.MEMBER_PORT AS nodePort,rgm.MEMBER_STATE AS nodeState,@@read_only AS readOnly,@@super_read_only AS superReadOnly,(CASE(SELECT VARIABLE_VALUE FROM `performance_schema`.`global_status` WHERE VARIABLE_NAME='group_replication_primary_member') WHEN '' THEN 'UNKOWN' WHEN rgm.MEMBER_ID THEN 'PRIMARY' ELSE 'STANDBY' END ) AS nodeRole FROM `performance_schema`.`replication_group_members` rgm";

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