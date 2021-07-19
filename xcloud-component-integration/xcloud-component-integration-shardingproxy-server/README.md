# XCloud Component for Sharding Proxy Server
> It's an enhanced package that integrates shardingsphere-proxy and shardingsphere-scaling


## Compile building

- Step1: First, building of `[shardingsphere](https://github.com/apache/shardingsphere)`

```
git clone https://github.com/apache/shardingsphere.git
cd shardingsphere
git checkout 5.0.0-beta
mvn clean install -DskipTests -Dmaven.test.skip=true -T 2C
```

- Step2: Building of `[xcloud-component](https://github.com/wl4g/xcloud-component)`

```
# git clone https://github.com/wl4g/xcloud-component.git
cd xcloud-component
mvn clean install -DskipTests -Dmaven.test.skip=true -T 2C
```

- Step3: Importion demo data

Directories:

```
├── demo_data
│   ├── group_sharding
│   │   ├── sharding1.jpg
│   │   ├── sharding2.jpg
│   │   └── userdb-sharding.sql
│   └── sharding
│       └── userdb-sharding.sql
```

> Notes: The example of non average slicing is not recommended for production (scenario: slicing according to different machine performance weight), because shardingsphere:5.0.0-alpha, It is recommended to use average sharding.


- Step4: Startup shardingsphere proxy(v4 and v5 Choose one)  

Startup classes:

```
com.wl4g.ShardingsphereProxy4
com.wl4g.ShardingsphereProxy5
```


## Failover

- `[MySQL5.7 Group Replication](https://dev.mysql.com/doc/refman/5.7/en/group-replication.html)` implementation theory

```sql
SELECT
    rgm.CHANNEL_NAME,
    rgm.MEMBER_ID,
    rgm.MEMBER_HOST,
    rgm.MEMBER_PORT,
    rgm.MEMBER_STATE,
    @@read_only AS READ_ONLY,
    @@super_read_only AS SUPER_READ_ONLY,
    ( CASE (SELECT VARIABLE_VALUE FROM `performance_schema`.`global_status` WHERE VARIABLE_NAME = 'group_replication_primary_member')
      WHEN rgm.MEMBER_ID THEN 'PRIMARY'
      ELSE 'SECONDARY' END
    ) AS MEMBER_ROLE
FROM
    `performance_schema`.`replication_group_members` rgm

```

For example result:

```table
group_replication_applier  05e9eb4f-9dec-11eb-8b2e-c0b5d741e9d5  wanglsir-pro  13307 ONLINE  0  0  SECONDARY
group_replication_applier  3d4ed671-9dec-11eb-9723-c0b5d741e9d5  wanglsir-pro  13308 ONLINE  0  0  SECONDARY
group_replication_applier  eb838b34-9deb-11eb-8677-c0b5d741e9d5  wanglsir-pro  13306 ONLINE  0  0  PRIMARY
```


## FQA

- Read write Split or sharding support different types of databases?

> Under the same schemaName, multiple sharding databases must be the same. See source code: [org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData](https://github.com/apache/shardingsphere/blob/5.0.0-beta/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/ShardingSphereMetaData.java#L35) and [org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource](https://github.com/apache/shardingsphere/blob/5.0.0-beta/shardingsphere-infra/shardingsphere-infra-common/src/main/java/org/apache/shardingsphere/infra/metadata/resource/ShardingSphereResource.java#L43)


