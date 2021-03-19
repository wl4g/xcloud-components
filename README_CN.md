# 一个基于Spring Boot/Cloud企业级通用组件。

### 目录说明

```
├── xcloud-component-bom # xcloud-component公共依赖
├── xcloud-component-common # 常用Utils、Helpers等, 如 SSH2Holders/SnowflakeIdGenerator/Encodes/ClassPathResourcePatternResolver等
├── xcloud-component-common-shade # xcloud-component-common模块的集成包
├── xcloud-component-core # 基于springboot体系特性增强, 如增强springMvc请求版本映射、统一异常处理、框架式自动配置spring.config.name等
├── xcloud-component-data # DB/Mybatis相关封装, 如mybatis热加载、多数据源等
├── xcloud-component-opencv # opencv的java版封装, 使在spring应用直接执行视觉功能任务成为可能
├── xcloud-component-rpc # 基于springboot/cloud分布式架构的集成与封装, 支持多种框架如dubbo/springcloud-feign/springboot-servicemesh快速切换等
│   ├── xcloud-component-rpc-example # 基于springboot/cloud分布式架构示例项目
│   ├── xcloud-component-rpc-feign-common # 为使如下springboot+feign、springcloud+feign等不同整合架构可轻易切换,可能会共用一些如@HystrixCommand/@FeignClient注解等公共部分
│   ├── xcloud-component-rpc-feign-core # springboot+feign架构整合封装
│   ├── xcloud-component-rpc-feign-istio # springboot+istio架构整合封装
│   ├── xcloud-component-rpc-feign-springcloud # springcloud+feign架构整合封装
│   ├── xcloud-component-rpc-feign-springcloud-dubbo # springcloud+feign+dubbo架构整合封装
│   ├── xcloud-component-rpc-springcloud-eureka-server # springcloud+eureka-server架构整合封装
│   └── xcloud-component-rpc-springcloud-hystrix-turbine-server # springcloud+hystrix+turbine整合封装
└── xcloud-component-support # 常用springboot应用组件封装, 如redisOperator(支持单机集群并存)、支持超时的分布式命令行器等
```

