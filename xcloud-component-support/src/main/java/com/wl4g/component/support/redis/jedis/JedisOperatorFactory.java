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
package com.wl4g.component.support.redis.jedis;

import static com.wl4g.component.common.collection.CollectionUtils2.safeList;
import static com.wl4g.component.common.lang.Assert2.*;
import static java.lang.String.format;
import static java.util.Objects.nonNull;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import com.wl4g.component.support.config.JedisAutoConfiguration.JedisProperties;

import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;

/**
 * Bean factory of {@link JedisOperator}.
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月13日
 * @since
 */
public class JedisOperatorFactory implements InitializingBean {
	protected final Logger log = LoggerFactory.getLogger(getClass());

	/**
	 * {@link JedisProperties}
	 */
	protected final JedisProperties config;

	/**
	 * {@link JedisCluster}
	 */
	protected final JedisCluster jedisCluster;

	/**
	 * {@link JedisPool}
	 */
	protected final JedisPool jedisPool;

	/**
	 * {@link JedisOperator}
	 */
	private JedisOperator jedisOperator;

	public JedisOperatorFactory(JedisCluster jedisCluster, JedisPool jedisPool) {
		notNullOf(jedisCluster, "jedisCluster");
		notNullOf(jedisPool, "jedisPool");
		this.config = null;
		this.jedisCluster = jedisCluster;
		this.jedisPool = jedisPool;
	}

	public JedisOperatorFactory(JedisProperties config) {
		notNullOf(config, "jedisProperties");
		this.config = config;
		this.jedisCluster = null;
		this.jedisPool = null;
	}

	public JedisOperatorFactory(JedisProperties config, JedisCluster jedisCluster, JedisPool jedisPool) {
		// notNullOf(config, "jedisProperties");
		// notNullOf(jedisCluster, "jedisCluster");
		// notNullOf(jedisPool, "jedisPool");
		this.config = config;
		this.jedisCluster = jedisCluster;
		this.jedisPool = jedisPool;
	}

	/**
	 * Gets {@link JedisOperator}
	 * 
	 * @return
	 */
	public JedisOperator getJedisOperator() {
		return jedisOperator;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
		log.info("Initialized jedis of: {}", jedisOperator);
	}

	/**
	 * Create a new {@link JedisOperator} instance in a composite way.
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception {
		if (nonNull(jedisCluster)) {
			jedisOperator = new DelegateJedisCluster(jedisCluster);
			log.info("Use already initialized jedis cluster: {}, finally auto adapt instantiation as: {}", jedisCluster,
					jedisOperator);
		} else if (nonNull(jedisPool)) {
			jedisOperator = new DelegateJedis(jedisPool, false);
			log.info("Use already initialized jedis pool: {}, finally auto adapt instantiation as: {}", jedisPool, jedisOperator);
		} else {
			notNull(config,
					"Cannot to automatically instantiate the %s. One of %s, %s and %s, expected at least 1 bean which qualifies as autowire candidate",
					JedisOperator.class.getSimpleName(), JedisPool.class.getSimpleName(), JedisCluster.class.getSimpleName(),
					JedisProperties.class.getSimpleName());
			initForConfiguration(config);
		}
	}

	/**
	 * New create {@link JedisOperator} instance with {@link JedisProperties}
	 * 
	 * @param config
	 * @throws Exception
	 */
	private void initForConfiguration(JedisProperties config) throws Exception {
		// Parse cluster node's
		Set<HostAndPort> nodes = config.parseHostAndPort();
		notEmpty(nodes, "Redis nodes configuration is requires, must contain at least 1 node");
		// nodes.forEach(n -> log.info("Connecting to redis node: {}", n));
		log.info("Connecting to redis nodes configuration: {}", config.toString());

		try {
			if (isCluster()) { // cluster?
				jedisOperator = new EnhancedJedisCluster(nodes, config.getConnTimeout(), config.getSoTimeout(),
						config.getMaxAttempts(), config.getPasswd(), config.getPoolConfig(), config.isSafeMode());
			} else { // single
				HostAndPort hap = nodes.iterator().next();
				JedisPool pool = new JedisPool(config.getPoolConfig(), hap.getHost(), hap.getPort(), config.getConnTimeout(),
						config.getSoTimeout(), config.getPasswd(), 0, config.getClientName(), false, null, null, null);
				jedisOperator = new DelegateJedis(pool, config.isSafeMode());
			}
			log.info("Use jedis configuration initialized instantiation as: {}", jedisOperator);
		} catch (Exception e) {
			throw new IllegalStateException(format("Cannot connect to redis nodes: %s", nodes), e);
		}

	}

	/**
	 * Check current config in {@link JedisCluster} mode.
	 * 
	 * @return
	 */
	private final boolean isCluster() {
		return safeList(config.getNodes()).size() > 1;
	}

}