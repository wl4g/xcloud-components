# Spring Boot Feign (XCloud)

A better feign client library to combine with `SpringBoot`, it can be used for migration 
from the bloated `SpringCloud` architecture to the `SpringBoot` + [istio](https://istio.io) architecture.


---



Write [Feign](https://github.com/OpenFeign/feign) client with `annotation`, like this:


On the basis of `@SpringBootApplication`:

```java
@EnableSpringBootFeignClients(basePackages = "com.wl4g.component.rpc.springboot.feign")
@SpringBootApplication
public class SpringBootTests {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}
```

#### Case1:

We can provide an interface using `@RequestLine` annotations.

```java
@SpringBootFeignClient(name = "github", url = "${github.url}")
public interface GithubService1 {

    @RequestLine("GET /repos/{owner}/{repo}/contributors")
    List<GitHubContributor> getContributors(@Param("owner") String owner, @Param("repo") String repo);
}
```

Now we can use it as we normally use `Spring`.

```java
    @Autowired
    private GithubService1 githubService1;
    
    List<GitHubContributor> contributors = githubService1.getContributors("wl4g", "xcloud-components");
    logger.info("contributors={}", new Gson().toJson(contributors));    
```

#### Case2:

We can provide an interface using `@RequestMapping` annotations. 
##### Note: that it must be configured to `SpringMvcContract`

```java
@SpringBootFeignClient(name = "github", url = "${github.url}", configuration = { SpringMvcContract.class })
public interface GithubService2 {

    @RequestMapping(method = GET, path = "/users/{owner}/repos")
    List<GitHubRepo> getRepos(@PathVariable("owner") String owner);

```

Now we can use it as we normally use `Spring`.

```java
    @Autowired
    private GithubService2 githubService2;

    List<GitHubRepo> repos = githubService2.getRepos("wl4g");
    logger.info("repos={}", new Gson().toJson(repos));    
```

##### For example codes refer to: [src/test/com/wl4g/component/rpc/springboot/feign/SpringBootFeignTests.java](src/test/com/wl4g/component/rpc/springboot/feign/SpringBootFeignTests.java)


## More features:
- You can also easily pan to the spring cloud feign environment without modifying the annotation, 
because `@SpringBootFeignClient` is compatible with `@FeignClient`



## More configuration

```properties
spring.boot.xcloud.feign.max-idle-connections = 520
spring.boot.xcloud.feign.connect-timeout = 11000
spring.boot.xcloud.feign.read-timeout = 12000
# Option: http2Client|okhttp3, Default: okhttp3
spring.boot.xcloud.feign.client.provider=http2Client
```