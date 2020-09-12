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
package com.wl4g.components.common.view;

import static com.wl4g.components.common.collection.Collections2.safeList;
import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static java.util.Objects.isNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.wl4g.components.common.annotation.Nullable;
import com.wl4g.components.common.collection.CollectionUtils2;
import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.common.resource.StreamResource;
import com.wl4g.components.common.resource.resolver.DefaultResourceLoader;
import com.wl4g.components.common.resource.resolver.ResourceLoader;

import freemarker.cache.FileTemplateLoader;
import freemarker.cache.MultiTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.SimpleHash;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * {@link FreemarkerUtils}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-12
 * @sine v1.0.0
 * @see {@link org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer}
 */
public abstract class FreemarkerUtils {

	protected static final SmartLogger log = getLogger(FreemarkerUtils.class);

	/**
	 * Process the specified FreeMarker template with the given model and write
	 * the result to the given Writer.
	 * 
	 * @param model
	 *            the model object, typically a Map that contains model names as
	 *            keys and model objects as values
	 * @return the result as String
	 * @throws IOException
	 *             if the template wasn't found or couldn't be read
	 * @throws freemarker.template.TemplateException
	 *             if rendering failed
	 */
	public static String processTemplateIntoString(Template template, Object model) throws IOException, TemplateException {
		StringWriter result = new StringWriter();
		template.process(model, result);
		return result.toString();
	}

	/**
	 * Prepare the FreeMarker Configuration and return it.
	 * 
	 * @param freemarkerSettings
	 * @param templateLoaderPaths
	 * @return the FreeMarker Configuration object
	 */
	public static Configuration createDefaultConfiguration(@Nullable Properties freemarkerSettings,
			@Nullable String... templateLoaderPaths) {
		Properties settings = new Properties();
		settings.setProperty("template_update_delay", "0");
		settings.setProperty("default_encoding", "UTF-8");
		settings.setProperty("number_format", "0.####");
		settings.setProperty("datetime_format", "yyyy-MM-dd HH:mm:ss");
		settings.setProperty("classic_compatible", "true");
		settings.setProperty("template_exception_handler", "ignore");

		// override
		if (!isNull(freemarkerSettings)) {
			settings.putAll(freemarkerSettings);
		}

		try {
			return createConfiguration(null, settings, null, null, null, null, templateLoaderPaths);
		} catch (IOException | TemplateException e) {
			throw new IllegalStateException(e);
		}
	}

	/**
	 * Prepare the FreeMarker Configuration and return it.
	 * 
	 * @param configLocation
	 * @param freemarkerSettings
	 * @param freemarkerVariables
	 * @param preTemplateLoaders
	 * @param templateLoaders
	 * @param postTemplateLoaders
	 * @param templateLoaderPaths
	 * @return the FreeMarker Configuration object
	 * @throws IOException
	 *             if the config file wasn't found
	 * @throws TemplateException
	 *             on FreeMarker initialization failure
	 */
	public static Configuration createConfiguration(@Nullable StreamResource configLocation,
			@Nullable Properties freemarkerSettings, @Nullable Map<String, Object> freemarkerVariables,
			@Nullable List<TemplateLoader> preTemplateLoaders, @Nullable List<TemplateLoader> templateLoaders,
			@Nullable List<TemplateLoader> postTemplateLoaders, @Nullable String... templateLoaderPaths)
			throws IOException, TemplateException {

		Configuration config = new Configuration(Configuration.DEFAULT_INCOMPATIBLE_IMPROVEMENTS);
		Properties props = new Properties();

		// Load config file if specified.
		if (configLocation != null) {
			log.debug("Loading FreeMarker configuration from " + configLocation);
			fillProperties(props, configLocation);
		}

		// Merge local properties if specified.
		if (freemarkerSettings != null) {
			props.putAll(freemarkerSettings);
		}

		// FreeMarker will only accept known keys in its setSettings and
		// setAllSharedVariables methods.
		if (!props.isEmpty()) {
			config.setSettings(props);
		}

		if (!CollectionUtils2.isEmpty(freemarkerVariables)) {
			config.setAllSharedVariables(new SimpleHash(freemarkerVariables, config.getObjectWrapper()));
		}
		config.setDefaultEncoding("UTF-8");

		List<TemplateLoader> tplLoaders = new ArrayList<>(safeList(templateLoaders));

		// Register default template loaders.
		if (templateLoaderPaths != null) {
			for (String path : templateLoaderPaths) {
				tplLoaders.add(getTemplateLoaderForPath(path));
			}
		}

		// Register template loaders that are supposed to kick in late.
		if (postTemplateLoaders != null) {
			tplLoaders.addAll(postTemplateLoaders);
		}

		TemplateLoader loader = getAggregateTemplateLoader(tplLoaders);
		if (loader != null) {
			config.setTemplateLoader(loader);
		}

		return config;
	}

	/**
	 * Determine a FreeMarker TemplateLoader for the given path.
	 * <p>
	 * Default implementation creates either a FileTemplateLoader or a
	 * ResourceTemplateLoader.
	 * 
	 * @param templateLoaderPath
	 *            the path to load templates from
	 * @return an appropriate TemplateLoader
	 * @see freemarker.cache.FileTemplateLoader
	 * @see ResourceTemplateLoader
	 */
	private static TemplateLoader getTemplateLoaderForPath(String templateLoaderPath) {
		// Try to load via the file system, fall back to
		// ResourceTemplateLoader
		// (for hot detection of template changes, if possible).
		try {
			StreamResource path = defaultResourceLoader.getResource(templateLoaderPath);
			File file = path.getFile(); // will fail if not resolvable in
										// the file system
			log.debug("Template loader path [" + path + "] resolved to file path [" + file.getAbsolutePath() + "]");
			return new FileTemplateLoader(file);
		} catch (Exception ex) {
			log.debug("Cannot resolve template loader path [" + templateLoaderPath
					+ "] to [java.io.File]: using ResourceTemplateLoader as fallback", ex);
			return new ResourceTemplateLoader(defaultResourceLoader, templateLoaderPath);
		}

	}

	private static TemplateLoader getAggregateTemplateLoader(List<TemplateLoader> templateLoaders) {
		switch (templateLoaders.size()) {
		case 0:
			log.debug("No FreeMarker TemplateLoaders specified");
			return null;
		case 1:
			return templateLoaders.get(0);
		default:
			TemplateLoader[] loaders = templateLoaders.toArray(new TemplateLoader[0]);
			return new MultiTemplateLoader(loaders);
		}
	}

	/**
	 * Fill the given properties from the given resource (in ISO-8859-1
	 * encoding).
	 * 
	 * @param props
	 *            the Properties instance to fill
	 * @param resource
	 *            the resource to load from
	 * @throws IOException
	 *             if loading failed
	 */
	private static void fillProperties(Properties props, StreamResource resource) throws IOException {
		InputStream is = resource.getInputStream();
		try {
			String filename = resource.getFilename();
			if (filename != null && filename.endsWith(".xml")) {
				props.loadFromXML(is);
			} else {
				props.load(is);
			}
		} finally {
			is.close();
		}
	}

	/**
	 * FreeMarker {@link TemplateLoader} adapter that loads via a
	 * {@link ResourceLoader}. for any resource loader path that cannot be
	 * resolved to a {@link java.io.File}.
	 */
	private static class ResourceTemplateLoader implements TemplateLoader {

		protected static final SmartLogger log = getLogger(ResourceTemplateLoader.class);

		private final ResourceLoader resourceLoader;

		private final String templateLoaderPath;

		/**
		 * Create a new ResourceTemplateLoader.
		 * 
		 * @param resourceLoader
		 *            the ResourceLoader to use
		 * @param templateLoaderPath
		 *            the template loader path to use
		 */
		public ResourceTemplateLoader(ResourceLoader resourceLoader, String templateLoaderPath) {
			this.resourceLoader = resourceLoader;
			if (!templateLoaderPath.endsWith("/")) {
				templateLoaderPath += "/";
			}
			this.templateLoaderPath = templateLoaderPath;
			log.debug("ResourceTemplateLoader for FreeMarker: using resource loader [" + this.resourceLoader
					+ "] and template loader path [" + this.templateLoaderPath + "]");
		}

		@Override
		@Nullable
		public Object findTemplateSource(String name) throws IOException {
			log.debug("Looking for FreeMarker template with name [" + name + "]");
			StreamResource resource = this.resourceLoader.getResource(this.templateLoaderPath + name);
			return (resource.exists() ? resource : null);
		}

		@Override
		public Reader getReader(Object templateSource, String encoding) throws IOException {
			StreamResource resource = (StreamResource) templateSource;
			try {
				return new InputStreamReader(resource.getInputStream(), encoding);
			} catch (IOException ex) {
				log.debug("Could not find FreeMarker template: " + resource);
				throw ex;
			}
		}

		@Override
		public long getLastModified(Object templateSource) {
			StreamResource resource = (StreamResource) templateSource;
			try {
				return resource.lastModified();
			} catch (IOException ex) {
				log.debug("Could not obtain last-modified timestamp for FreeMarker template in " + resource + ": " + ex);
				return -1;
			}
		}

		@Override
		public void closeTemplateSource(Object templateSource) throws IOException {
		}

	}

	private static final ResourceLoader defaultResourceLoader = new DefaultResourceLoader();

}
