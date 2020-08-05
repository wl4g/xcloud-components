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
package com.wl4g.components.data.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.support.http.StatViewServlet;
import com.alibaba.druid.support.http.WebStatFilter;
import com.wl4g.components.common.codec.CodecSource;
import com.wl4g.components.common.crypto.symmetric.AES128ECBPKCS5;

import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;

import static java.lang.String.format;
import static java.lang.String.valueOf;

import javax.sql.DataSource;
import java.sql.SQLException;

/**
 * DataSource configuration
 * 
 * @author Wangl.sir <983708408@qq.com>
 * @version v1.0
 * @date 2018年11月13日
 * @since
 */
public class DruidAutoConfiguration extends AbstractDataSourceAutoConfiguration {

	@Bean
	@RefreshScope
	@ConditionalOnMissingBean
	public DruidDataSource druidDataSource(DruidProperties config) {
		DruidDataSource druid = new DruidDataSource();
		druid.setUrl(config.getUrl());
		druid.setUsername(config.getUsername());
		String plain = config.getPassword();
		if (valueOf(environment.getProperty("spring.profiles.active")).startsWith("pro")) {
			try {
				// TODO using dynamic cipherKey??
				byte[] cipherKey = AES128ECBPKCS5.getEnvCipherKey("DEVOPS_CIPHER_KEY");
				plain = new AES128ECBPKCS5().decrypt(cipherKey, CodecSource.fromHex(config.getPassword())).toString();
			} catch (Throwable th) {
				throw new IllegalStateException(format("Unable to decryption database password for '%s'", config.getPassword()),
						th);
			}
		}
		druid.setPassword(plain);
		druid.setDriverClassName(config.getDriverClassName());
		druid.setInitialSize(config.getInitialSize());
		druid.setMinIdle(config.getMinIdle());
		druid.setMaxActive(config.getMaxActive());
		druid.setMaxWait(config.getMaxWait());
		druid.setTimeBetweenEvictionRunsMillis(config.getTimeBetweenEvictionRunsMillis());
		druid.setMinEvictableIdleTimeMillis(config.getMinEvictableIdleTimeMillis());
		druid.setValidationQuery(config.getValidationQuery());
		druid.setTestWhileIdle(config.isTestWhileIdle());
		druid.setTestOnBorrow(config.isTestOnBorrow());
		druid.setTestOnReturn(config.isTestOnReturn());
		try {
			druid.setFilters(config.getFilters());
		} catch (SQLException e) {
			log.error("Cannot initialization druid filter", e);
		}
		return druid;
	}

	@Bean
	public SqlSessionFactoryBean multiSqlSessionFactoryBean(DataSource dataSource, MybatisProperties mybatisConfig)
			throws Exception {
		return createMultiSqlSessionFactoryBean(dataSource, mybatisConfig);
	}

	@Bean
	public ServletRegistrationBean<StatViewServlet> druidStatViewServlet(DruidProperties druidConfig) {
		ServletRegistrationBean<StatViewServlet> registrar = new ServletRegistrationBean<>();
		registrar.setServlet(new StatViewServlet());
		registrar.addUrlMappings("/druid/*");
		registrar.addInitParameter("loginUsername", druidConfig.getWebLoginUsername());
		registrar.addInitParameter("loginPassword", druidConfig.getWebLoginPassword());
		registrar.addInitParameter("logSlowSql", druidConfig.getLogSlowSql());
		return registrar;
	}

	@Bean
	public FilterRegistrationBean<WebStatFilter> druidWebStatFilter() {
		FilterRegistrationBean<WebStatFilter> registrar = new FilterRegistrationBean<>();
		registrar.setFilter(new WebStatFilter());
		registrar.addUrlPatterns("/druid/*");
		registrar.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
		registrar.addInitParameter("profileEnable", "true");
		return registrar;
	}

	@Bean
	public DruidProperties druidProperties() {
		return new DruidProperties();
	}

	/**
	 * {@link DruidProperties}
	 *
	 * @since
	 */
	@ConfigurationProperties(prefix = "spring.datasource.druid")
	public static class DruidProperties {

		private String url;
		private String username;
		private String password;
		private String driverClassName;
		private int initialSize;
		private int minIdle;
		private int maxActive;
		private int maxWait;
		private int timeBetweenEvictionRunsMillis;
		private int minEvictableIdleTimeMillis;
		private String validationQuery;
		private boolean testWhileIdle;
		private boolean testOnBorrow;
		private boolean testOnReturn;
		private String filters;

		private String logSlowSql;
		private String webLoginUsername = "druid";
		private String webLoginPassword = "druid";

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getUsername() {
			return username;
		}

		public void setUsername(String username) {
			this.username = username;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public String getDriverClassName() {
			return driverClassName;
		}

		public void setDriverClassName(String driverClassName) {
			this.driverClassName = driverClassName;
		}

		public int getInitialSize() {
			return initialSize;
		}

		public void setInitialSize(int initialSize) {
			this.initialSize = initialSize;
		}

		public int getMinIdle() {
			return minIdle;
		}

		public void setMinIdle(int minIdle) {
			this.minIdle = minIdle;
		}

		public int getMaxActive() {
			return maxActive;
		}

		public void setMaxActive(int maxActive) {
			this.maxActive = maxActive;
		}

		public int getMaxWait() {
			return maxWait;
		}

		public void setMaxWait(int maxWait) {
			this.maxWait = maxWait;
		}

		public int getTimeBetweenEvictionRunsMillis() {
			return timeBetweenEvictionRunsMillis;
		}

		public void setTimeBetweenEvictionRunsMillis(int timeBetweenEvictionRunsMillis) {
			this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
		}

		public int getMinEvictableIdleTimeMillis() {
			return minEvictableIdleTimeMillis;
		}

		public void setMinEvictableIdleTimeMillis(int minEvictableIdleTimeMillis) {
			this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
		}

		public String getValidationQuery() {
			return validationQuery;
		}

		public void setValidationQuery(String validationQuery) {
			this.validationQuery = validationQuery;
		}

		public boolean isTestWhileIdle() {
			return testWhileIdle;
		}

		public void setTestWhileIdle(boolean testWhileIdle) {
			this.testWhileIdle = testWhileIdle;
		}

		public boolean isTestOnBorrow() {
			return testOnBorrow;
		}

		public void setTestOnBorrow(boolean testOnBorrow) {
			this.testOnBorrow = testOnBorrow;
		}

		public boolean isTestOnReturn() {
			return testOnReturn;
		}

		public void setTestOnReturn(boolean testOnReturn) {
			this.testOnReturn = testOnReturn;
		}

		public String getFilters() {
			return filters;
		}

		public void setFilters(String filters) {
			this.filters = filters;
		}

		public String getLogSlowSql() {
			return logSlowSql;
		}

		public void setLogSlowSql(String logSlowSql) {
			this.logSlowSql = logSlowSql;
		}

		public String getWebLoginUsername() {
			return webLoginUsername;
		}

		public void setWebLoginUsername(String webLoginUsername) {
			this.webLoginUsername = webLoginUsername;
		}

		public String getWebLoginPassword() {
			return webLoginPassword;
		}

		public void setWebLoginPassword(String webLoginPassword) {
			this.webLoginPassword = webLoginPassword;
		}

	}

	static {
		// com.alibaba.druid.support.logging.LogFactory.static{}
		System.setProperty("druid.logType", "slf4j");
	}

}