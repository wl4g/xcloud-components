## Shardingsphere Server
> It's an enhanced package that integrates shardingsphere-proxy and shardingsphere-scaling


#### Developer's guide

- Step1: First building the project `shardingsphere`

```
git clone https://github.com/apache/shardingsphere.git
cd shardingsphere
git checkout 4.1.1
mvn clean install -DskipTests -Dmaven.test.skip=true -T 2C
```

- Step2: Building `xcloud-component`

```
# git clone https://github.com/wl4g/xcloud-component.git
cd xcloud-component
mvn clean install -DskipTests -Dmaven.test.skip=true -T 2C
```

- Step3: Importion demo data

Directory structure:

```
demo_data
  sharding # (Recommends)
    config-sharding-userdb.yaml
    userdb-sharding.sql
  group_sharding # (Alpha)
    config-sharding-userdb.yaml
    sharding1.jpg
    sharding2.jpg
    userdb-sharding.sql
```
> Notes: The example of non average slicing is not recommended for production (scenario: slicing according to different machine performance weight), because shardingsphere:5.0.0-alpha, It is recommended to use average sharding.

