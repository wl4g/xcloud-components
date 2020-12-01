/*
 * Copyright 2017 ~ 2025 the original author or authors. <wanglsir@gmail.com, 983708408@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wl4g.components.core.web.versions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.wl4g.components.core.web.method.mapping.WebMvcHandlerMappingConfigurer;
import com.wl4g.components.core.web.versions.annotation.ApiVersion;
import com.wl4g.components.core.web.versions.annotation.ApiVersionGroup;
import com.wl4g.components.core.web.versions.annotation.EnableApiVersionMapping;

/**
 * {@link ApiVersionControllerTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-01
 * @sine v1.0
 * @see https://spring.io/guides/gs/testing-web/
 */
@EnableApiVersionMapping
@SpringBootApplication(scanBasePackageClasses = { WebMvcHandlerMappingConfigurer.class })
public class ApiVersionControllerTests {

	public static void main(String[] args) {
		SpringApplication.run(ApiVersionControllerTests.class, args);

		System.out.println("----------- Testing ------------");
		testRequest("http://localhost:8080/api-test/userinfo?version=1.0.10.2a&platform=iOS");
		testRequest("http://localhost:8080/api-test/userinfo?version=1.0.10.2b&platform=iOS");

		System.exit(0);
	}

	static void testRequest(String url) {
		String result = new RestTemplate().getForObject(url, String.class);
		System.out.println("result: " + result);
	}

	@RestController
	@RequestMapping("/api-test/")
	public static class TestApiVersionController {

		@ApiVersionGroup(@ApiVersion(clients = { "iOS" }, value = "1.0.10.2a"))
		@RequestMapping("userinfo")
		public String userinfoV1_0_10_2a() {
			return "My name is jack (V1_0_10_2a)";
		}

		@ApiVersionGroup(@ApiVersion(clients = { "iOS" }, value = "1.0.10.2b"))
		@RequestMapping("userinfo")
		public String userinfoV1_0_10_2b() {
			return "My name is jack (V1_0_10_2b)";
		}

	}

}
