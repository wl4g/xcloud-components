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

import static org.assertj.core.api.Assertions.assertThat;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;

/**
 * {@link ApiVersionControllerTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-01
 * @sine v1.0
 * @see https://spring.io/guides/gs/testing-web/
 */
// @RunWith(SpringRunner.class)
// @WebMvcTest(com.wl4g.components.core.web.versions.ApiVersionControllerTests.TestApiVersionController.class)
// @SpringBootTest
// @AutoConfigureMockMvc
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
// (classes = ApiVersionControllerTests.class)
// @AutoConfigureMockMvc
// @ActiveProfiles
public class ApiVersionControllerTests {

	// private MockMvc mockMvc;
	//
	// @Autowired
	// private WebApplicationContext webApplicationContext;
	//
	// @Before
	// public void init() {
	// // Option1:
	// // this.mockMvc = MockMvcBuilders.standaloneSetup(new
	// // TestApiVersionController()).build();
	// // Option2:
	// this.mockMvc =
	// MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	// }

	// @Test
	// public void userinfoCase1() throws Exception {
	// this.mockMvc.perform(get("/apt-test/userinfo").accept(MediaType.APPLICATION_JSON)).andDo(print())
	// .andExpect(status().isOk()).andExpect(content().string(containsString("My
	// name is jack")));
	// }

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void userinfoCase1() throws Exception {
		assertThat(restTemplate.getForObject("http://localhost:" + port + "/api-test/userinfo", String.class))
				.contains("My name is jack");
	}

	@RestController
	@RequestMapping("/api-test/")
	public static class TestApiVersionController {

		@RequestMapping("userinfo")
		public String userinfo() {
			return "My name is jack";
		}

	}

}
