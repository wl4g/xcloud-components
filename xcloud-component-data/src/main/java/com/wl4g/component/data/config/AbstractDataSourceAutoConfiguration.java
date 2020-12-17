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
package com.wl4g.component.data.config;

import static com.wl4g.component.common.collection.Collections2.safeList;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.ibatis.plugin.Interceptor;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import static org.springframework.core.io.support.ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;
import static org.springframework.util.ClassUtils.convertClassNameToResourcePath;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.data.mybatis.session.SmartSqlSessionFactoryBean;

/**
 * {@link AbstractDataSourceAutoConfiguration}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020-08-05
 * @since
 */
public abstract class AbstractDataSourceAutoConfiguration {

	protected final SmartLogger log = getLogger(getClass());

	@Autowired
	protected Environment environment;

	/**
	 * New create {@link MultiSqlSessionFactoryBean} instance
	 * 
	 * @param config
	 * @param dataSource
	 * @param interceptors
	 * @return
	 * @throws Exception
	 */
	protected SqlSessionFactoryBean createSmartSqlSessionFactoryBean(MybatisProperties config, DataSource dataSource,
			List<Interceptor> interceptors) throws Exception {
		// Class path matcher resolver.
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

		// Build SqlSessionFactory
		SqlSessionFactoryBean factory = new SmartSqlSessionFactoryBean();
		factory.setDataSource(dataSource);
		factory.setTypeAliases(getTypeAliases(resolver, config));
		factory.setConfigLocation(new ClassPathResource(config.getConfigLocation()));
		factory.setMapperLocations(getDaoMappers(resolver, config));

		// Plugin interceptors sorting.
		AnnotationAwareOrderComparator.sort(interceptors);
		factory.setPlugins(interceptors.toArray(new Interceptor[0]));

		return factory;
	}

	/**
	 * Let typeAliasesPackage alias bean support wildcards.
	 * 
	 * @return
	 */
	private Class<?>[] getTypeAliases(PathMatchingResourcePatternResolver resolver, MybatisProperties config) throws Exception {
		List<Class<?>> typeAliases = new ArrayList<>(16);

		// Define metadataReader
		MetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(resolver);

		for (String pkg : safeList(config.getTypeAliasPackage())) {
			String location = CLASSPATH_ALL_URL_PREFIX + convertClassNameToResourcePath(pkg) + "**/*.class";
			Resource[] resources = resolver.getResources(location);
			if (resources != null) {
				for (Resource resource : resources) {
					if (resource.isReadable()) {
						MetadataReader metadataReader = readerFactory.getMetadataReader(resource);
						typeAliases.add(Class.forName(metadataReader.getClassMetadata().getClassName()));
					}
				}
			}
		}

		return typeAliases.toArray(new Class<?>[] {});
	}

	/**
	 * Gets scanning load DAO mappers configuration.
	 * 
	 * @param resolver
	 * @param config
	 * @return
	 * @throws Exception
	 */
	private Resource[] getDaoMappers(PathMatchingResourcePatternResolver resolver, MybatisProperties config) throws Exception {
		// Scanning load mappers all
		List<Resource> ress = new ArrayList<>(16);
		for (String location : safeList(config.getMapperLocations())) {
			try {
				ress.addAll(asList(resolver.getResources(location)));
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return ress.toArray(new Resource[] {});
	}

}
