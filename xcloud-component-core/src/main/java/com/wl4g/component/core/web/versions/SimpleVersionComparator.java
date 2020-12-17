package com.wl4g.component.core.web.versions;

import static com.wl4g.component.common.collection.Collections2.isEmptyArray;
import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.trimToEmpty;

import java.util.Comparator;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.wl4g.component.common.log.SmartLogger;

/**
 * Simple API version comparator with ASCII.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-30
 * @sine v1.0
 * @see https://www.cnblogs.com/yucongblog/p/5600312.html
 */
public class SimpleVersionComparator implements Comparator<String> {

	/**
	 * Default version comparator instance.
	 */
	public static final SimpleVersionComparator INSTANCE = new SimpleVersionComparator();

	protected final SmartLogger log = getLogger(getClass());

	protected final Pattern versionRegex;

	public SimpleVersionComparator() {
		this(DEFAULT_VERSION_REGEX);
	}

	public SimpleVersionComparator(@NotBlank String versionRegex) {
		this.versionRegex = Pattern.compile(hasTextOf(versionRegex, "versionRegex"));
	}

	@Override
	public int compare(@Nullable String version1, @Nullable String version2) {
		if (isNull(version1) || isNull(version2)) {
			return trimToEmpty(version1).compareTo(trimToEmpty(version2));
		}

		// Resolves version numbers
		String[] verParts1 = resolveApiVersionParts(version1, false);
		String[] verParts2 = resolveApiVersionParts(version2, false);

		// First, direct quick compare
		if (version1.compareTo(version2) == 0) {
			return 0;
		}

		// Check the size of the common parts from left to right with the least
		// number of iterations.
		int iter = Math.min(verParts1.length, verParts2.length);
		for (int i = 0; i < iter; i++) {
			final int compared = verParts1[i].compareTo(verParts2[i]);
			if (compared != 0) {
				return compared;
			}
		}

		// At this time, it must be different. Since the public sector can not
		// win, it is a long-term win.
		if (verParts1.length > verParts2.length) {
			return 1;
		}

		return -1;
	}

	/**
	 * Resolves version numbers with pattern.
	 * 
	 * @param version
	 * @param valid
	 * @return
	 */
	public String[] resolveApiVersionParts(@NotNull String version, boolean valid) {

		String[] verParts = versionRegex.split(version);
		if (!isBlank(version) && isEmptyArray(verParts)) {
			String errmsg = format(
					"Invalid version: '%s', Refer to for example: 1.10.0.2a or 1_10_0_2b or 1-10-0-2b etc, The delimiter should satisfy the version regex: '%s'",
					version, versionRegex);
			if (!valid) {
				log.warn(errmsg);
				return EMPTY_ARRAY;
			}
			throw new IllegalArgumentException(errmsg);
		}

		return verParts;
	}

	public static final String DEFAULT_VERSION_REGEX = "[-_./;:]";
	public static final String[] EMPTY_ARRAY = {};

}
