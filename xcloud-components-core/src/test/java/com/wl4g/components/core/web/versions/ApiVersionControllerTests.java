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

import static java.lang.String.format;
import static java.lang.System.out;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.wl4g.components.core.web.method.mapping.WebMvcHandlerMappingConfigurer;
import com.wl4g.components.core.web.versions.annotation.ApiVersion;
import com.wl4g.components.core.web.versions.annotation.ApiVersionMapping;
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
@SpringBootApplication(scanBasePackageClasses = WebMvcHandlerMappingConfigurer.class)
public class ApiVersionControllerTests {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(ApiVersionControllerTests.class, args);

		checkApiVersionsMatchsCase1();

		System.exit(0);
	}

	/**
	 * <pre>
	 * Version info parameter alias name includes: eg: '_v' or 'version' or 'apiVersion';
	 * 
	 * Client info parameter alias name includes, eg: 'platform' or 'clientType'
	 * 
	 * refer to {@link EnableApiVersionMapping}
	 * </pre>
	 */
	private static void checkApiVersionsMatchsCase1() {
		System.out.println("----------- Testing ------------");

		RestTemplate rest = new RestTemplate();
		String url = "http://localhost:8080/api-test/userinfo?_v=%s&platform=%s&%s=%s";

		out.println(">> " + rest.getForObject(format(url, "2.0.10.2a", "iOS", "token", "abcd111"), String.class));
		out.println(">> " + rest.getForObject(format(url, "2.0.10.2a", "iOS", "token", "abcd222"), String.class));
		out.println(">> " + rest.getForObject(format(url, "2.0.10.3a", "WebPC", "token", "abcd333"), String.class));
		out.println(">> " + rest.getForObject(format(url, "2.0.10.2a", "WebPC", "token", "abcd444"), String.class));
		out.println(">> " + rest.getForObject(format(url, "2.0.10.2b", "Android", "token", "abcd555"), String.class));
		out.println(">> " + rest.getForObject(format(url, "", "", "token", "abcd666"), String.class));

	}

	@RestController
	@RequestMapping("/api-test/")
	public static class TestApiVersionController {

		@ApiVersionMapping({ @ApiVersion(groups = { "iOS", "wechatmp" }, value = "1.0.10.2a"),
				@ApiVersion(groups = { "WebPC", "NativePC" }, value = "1.0.10.3a") })
		@RequestMapping("userinfo")
		public String getUserInfoV1_0_10_2a(String token) {
			return "I'am API, version is 'V1_0_10_2a' - token=" + token;
		}

		@ApiVersionMapping(@ApiVersion(groups = { "iOS", "Android" }, value = "2.0.10.2b"))
		@RequestMapping("userinfo")
		public String getUserInfoV2_0_10_2b(String token) {
			return "I'am API, version is 'V2_0_10_2b' - token=" + token;
		}

		// Default api
		@RequestMapping("userinfo")
		public String getUserInfo(String token) {
			return "I'am API, version is default - token=" + token;
		}

	}

}
