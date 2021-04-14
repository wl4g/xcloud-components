## Shardingsphere Proxy Plugin


#### Developer's guide

- Step1: First, building the project `shardingsphere`

```
git clone https://github.com/apache/shardingsphere.git
cd shardingsphere
mvn clean install -DskipTests -Dmaven.test.skip=true -T 2C
```

- Step2: Building `shardingsphere-proxy-plugin`

```
# git clone https://github.com/wl4g/xcloud-component.git
cd xcloud-component
mvn clean install -DskipTests -Dmaven.test.skip=true -T 2C
```

- Step3: [Importing demo database tables.](demo_data/userdb-sharding.sql)


- Step4: Use `shardingsphere-proxy-plugin`

```
cp target/xcloud-component-integration-shardingsphere-proxy-plugin.jar apache-shardingsphere-${version}-shardingsphere-proxy-bin/lib/
cp target/xcloud-component-integration-shardingsphere-proxy-plugin/demo_data/config-sharding-userdb.yaml apache-shardingsphere-${version}-shardingsphere-proxy-bin/conf/
./apache-shardingsphere-${version}-shardingsphere-proxy-bin/bin/start.sh
```

> Notes: the above is a pseudo command. Please make sure the path is correct during the actual execution.
