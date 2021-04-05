# XCloud Component Rpc Examples

> This example demonstrates how to use `xcloud-component-rpc` to quickly integrate feign based distributed architecture based on external integration locally.

### Quick start
- 1. [Start the Eureka service locally first](../xcloud-component-integration-springcloud-eureka-server/README.md)

- 2. Start [RpcExampleService](xcloud-component-integration-example-starter-service/src/main/java/com/wl4g/RpcExampleService.java) and [RpcExampleWeb](xcloud-component-integration-example-starter-web/src/main/java/com/wl4g/RpcExampleWeb.java)

- 3. Browser access testing: &nbsp; `http://localhost:27001/rpc-example-web/order/list`

```
{"code":200,"status":"Normal","requestId":null,"message":"Ok","data":[{"orderNo":10001,"name":"Sniper rifle","deliveryAddress":"1458 Bee Street1","attributes":null},{"orderNo":10002,"name":"Over limit combat check","deliveryAddress":"95 Oxford Rd","attributes":null},{"orderNo":10003,"name":"fake vote","deliveryAddress":"394 Patterson Fork Road","attributes":null}]}
```
