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

import static java.lang.System.out;

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

	/**
	 * <pre>
	 * Version info parameter alias name includes: eg: '_v' or 'version' or 'apiVersion';
	 * 
	 * Client info parameter alias name includes, eg: 'platform' or 'clientType'
	 * 
	 * refer to {@link EnableApiVersionMapping}
	 * </pre>
	 * 
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(ApiVersionControllerTests.class, args);

		// new Thread(() -> multiVersionApiCase1()).start();
		// Thread.sleep(60_000L);
		// System.exit(0);

		multiVersionApiCase1();
	}

	static void multiVersionApiCase1() {
		System.out.println("----------- Testing ------------");

		RestTemplate rest = new RestTemplate();
		String baseUri = "http://localhost:8080/api-test/userinfo";

		out.println("result: " + rest.getForObject(baseUri + "?_v=1.0.10.2a&platform=iOS&device1=aa123", String.class));
		out.println("result: " + rest.getForObject(baseUri + "?_v=2.0.10.2b&platform=iOS&device2=bb321", String.class));
		out.println("result: " + rest.getForObject(baseUri + "?_v=2.0.10.2b&platform=Android&device2=cc235", String.class));
	}

	@RestController
	@RequestMapping("/api-test/")
	public static class TestApiVersionController {

		@ApiVersionGroup(@ApiVersion(clients = { "iOS", "wechatmp" }, value = "1.0.10.2a"))
		@RequestMapping("userinfo")
		public String userinfoV1_0_10_2a(String device1) {
			return "I am the api, version: V1_0_10_2a - device1=" + device1;
		}

		@ApiVersionGroup(@ApiVersion(clients = { "iOS", "Android" }, value = "2.0.10.2b"))
		@RequestMapping("userinfo")
		public String userinfoV2_0_10_2b(String device2) {
			return "I am the api, version: V2_0_10_2b - device2=" + device2;
		}

	}

}
