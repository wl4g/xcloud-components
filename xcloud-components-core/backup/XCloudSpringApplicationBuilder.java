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
package com.wl4g.components.core.spring;

import java.util.Set;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.io.ResourceLoader;

/**
 * {@link XCloudSpringApplicationBuilder}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-23
 * @sine v1.0
 * @see
 */
public class XCloudSpringApplicationBuilder extends SpringApplicationBuilder {

	public XCloudSpringApplicationBuilder(Class<?>... sources) {
		super(sources);
	}

	@Override
	protected SpringApplication createSpringApplication(Class<?>... sources) {
		return new XCloudSpringApplication(sources);
	}

	/**
	 * {@link XCloudSpringApplication}
	 * 
	 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
	 * @version v1.0 2020-11-23
	 * @sine v1.0
	 * @see
	 */
	public static class XCloudSpringApplication extends SpringApplication {

		/**
		 * Create a new {@link SpringApplication} instance. The application
		 * context will load beans from the specified primary sources (see
		 * {@link XCloudSpringApplication class-level} documentation for
		 * details. The instance can be customized before calling
		 * {@link #run(String...)}.
		 * 
		 * @param primarySources
		 *            the primary bean sources
		 * @see #run(Class, String[])
		 * @see #XCloudSpringApplication(ResourceLoader, Class...)
		 * @see #setSources(Set)
		 */
		public XCloudSpringApplication(Class<?>... primarySources) {
			this(null, primarySources);
		}

		/**
		 * Create a new {@link XCloudSpringApplication} instance. The
		 * application context will load beans from the specified primary
		 * sources (see {@link XCloudSpringApplication class-level}
		 * documentation for details. The instance can be customized before
		 * calling {@link #run(String...)}.
		 * 
		 * @param resourceLoader
		 *            the resource loader to use
		 * @param primarySources
		 *            the primary bean sources
		 * @see #run(Class, String[])
		 * @see #setSources(Set)
		 */
		public XCloudSpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
			super(resourceLoader, primarySources);
		}

	}

}
