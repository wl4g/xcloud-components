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
package com.wl4g.components.support.redis.jedis;

import static java.lang.System.out;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

import org.junit.Test;

import com.wl4g.components.support.config.JedisAutoConfiguration.JedisProperties;
import com.wl4g.components.support.redis.jedis.JedisOperator;
import com.wl4g.components.support.redis.jedis.JedisOperatorFactory;
import com.wl4g.components.support.redis.jedis.JedisOperator.RedisProtoUtil;

/**
 * {@link JedisOperatorFactoryTests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020年7月18日 v1.0.0
 * @see
 */
public class JedisOperatorFactoryTests {

	@Test
	public void checkKeyFormatTest1() {
		System.out.println("-----11-----");
		RedisProtoUtil.checkArguments(asList("safecloud_support_appinfo_admin"));

		System.out.println("-----22-----");
		RedisProtoUtil.checkArguments(asList("3342701404111872&&800492841ab644dc8ea01c683a809255BELONGANNUPXIN"));

		try {
			System.out.println("-----33-----");
			RedisProtoUtil.checkArguments(asList("3342701404111872-800492841ab644dc8ea01c683a809255BELONGANNUPXIN"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("-----44-----");
		System.out.println(RedisProtoUtil.keyFormat("3342701404111872-800492841ab644dc8ea01c683a809255BELONGANNUPXIN"));
	}

	@Test
	public void createWithJedisSingleTest2() throws Exception {
		JedisProperties config = new JedisProperties();
		config.setNodes(singletonList("127.0.0.1:6379"));

		out.println("Instantiating composite operators adapter with single ...");
		JedisOperatorFactory factory = new JedisOperatorFactory(config);
		factory.afterPropertiesSet();
		JedisOperator operator = factory.getJedisOperator();

		out.printf("\nadapter.set() result: %s", operator.set("foo", "bar"));
		out.printf("\nadapter.get() result: %s", operator.get("foo"));

	}

	@Test
	public void createWithJedisClusterTest3() throws Exception {
		JedisProperties config = new JedisProperties();
		config.setNodes(asList(new String[] { "127.0.0.1:6379", "127.0.0.1:6380", "127.0.0.1:6381", "127.0.0.1:7379",
				"127.0.0.1:7380", "127.0.0.1:7381" }));
		config.setPasswd("123456");

		out.println("Instantiating composite operators adapter with cluster ...");
		JedisOperatorFactory factory = new JedisOperatorFactory(config);
		factory.afterPropertiesSet();
		JedisOperator operator = factory.getJedisOperator();

		out.printf("\nadapter.set() result: %s", operator.set("foo", "bar"));
		out.printf("\nadapter.get() result: %s", operator.get("foo"));

	}

}