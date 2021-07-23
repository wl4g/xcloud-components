# Enterprise level generic component based on spring boot/cloud.

### Directories structure

```
├── xcloud-component-bom # xcloud-component Public dependence
├── xcloud-component-common # Commonly used utils and helpers, e.g SSH2Holders/SnowflakeIdGenerator/Encodes/, etc
├── xcloud-component-common-shade # xcloud-component-common Integration package for modules
├── xcloud-component-core # Based on the spring boot system features enhancement, such as enhanced spring MVC request version mapping, unified exception handling, framework automatic configuration spring.config.name etc.
├── xcloud-component-data # Db/Mybatis related packages, such as mybatis hot loading, multi data sources, etc
├── xcloud-component-opencv # The Java version of OpenCV makes it possible to perform visual functions directly in spring applications
├── xcloud-component-rpc # Based on the integration and encapsulation of springboot / cloud distributed architecture, it supports a variety of frameworks, e.g: `springboot-feign/springcloud-feign/springcloud-dubbo/springboot-servicemesh` fast switching and so on
│   ├── xcloud-component-integration-example # Sample project of distributed architecture based on springboot/cloud
│   ├── xcloud-component-integration-feign-common # In order to make the following integration architectures such as springboot+feign、springcloud+feign easy to switch, some common parts such as @HystrixCommand/@FeignClient annotation may be shared.
│   ├── xcloud-component-integration-circuitbreaker-hystrix-turbine-server # springcloud+hystrix+turbine Architecture integration and encapsulation
│   ├── xcloud-component-integration-feign-core # springboot+feign Architecture integration and encapsulation
│   ├── xcloud-component-integration-feign-istio # springboot+istio Architecture integration and encapsulation
│   ├── xcloud-component-integration-feign-springcloud # springcloud+feign Architecture integration and encapsulation
│   ├── xcloud-component-integration-feign-springcloud-dubbo # springcloud+feign+dubbo Architecture integration and encapsulation
│   ├── xcloud-component-integration-feign-springcloud-seata # springcloud+feign+seata Architecture integration and encapsulation
│   ├── xcloud-component-integration-springcloud-eureka-server # springcloud+eureka-server Architecture integration and encapsulation
│   └── xcloud-component-integration-shardingproxy-server # shardingsphere customized enhanced version, such as supporting failover (master-slave automatic switching), etc
└── xcloud-component-support # The common springboot application component encapsulation, such as redisOperator(support for the coexistence of single cluster), distributed command-line device supporting timeout, etc
```

