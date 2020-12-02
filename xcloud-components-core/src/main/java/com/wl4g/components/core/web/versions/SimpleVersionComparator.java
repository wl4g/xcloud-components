package com.wl4g.components.core.web.versions;

import static com.wl4g.components.common.lang.Assert2.hasTextOf;
import static com.wl4g.components.common.lang.Assert2.isTrue;
import static java.lang.String.format;
import static java.lang.String.valueOf;
import static java.util.Objects.isNull;

import java.util.Comparator;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * Simple API version comparator with ASCII.
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-30
 * @sine v1.0
 */
public class SimpleVersionComparator implements Comparator<String> {

	// Default version comparator instance.
	public static final SimpleVersionComparator INSTANCE = new SimpleVersionComparator();

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
			// TODO
			return valueOf(version1).compareTo(valueOf(version2));
		}

		// Check version syntax.
		String[] verParts1 = checkSyntaxVersion(versionRegex, version1);
		String[] verParts2 = checkSyntaxVersion(versionRegex, version2);

		// First use ascii compare directly.
		if (version1.compareTo(version2) == 0) {
			return 0;
		}

		// Min number of iterations.
		int iter = Math.min(verParts1.length, verParts2.length);
		for (int i = 0; i < iter; i++) {
			if (verParts1[i].compareTo(verParts2[i]) > 0) {
				return 1;
			}
		}
		return -1;
	}

	/**
	 * Check version string with pattern.
	 * 
	 * @param version
	 * @return
	 */
	public String[] checkSyntaxVersion(@NotBlank String version) {
		return checkSyntaxVersion(versionRegex, version);
	}

	/**
	 * Check version string with pattern.
	 * 
	 * @param versionRegex
	 * @param version
	 * @return
	 */
	public static String[] checkSyntaxVersion(@NotNull Pattern versionRegex, @NotBlank String version) {
		hasTextOf(version, "version");
		String[] verParts = versionRegex.split(version);
		isTrue(verParts.length >= 2 && verParts.length <= 4,
				format("Invalid version syntax: '%s', Refer to the correct syntax version:"
						+ " {major}{delimiter}{minor}[{delimiter}{revision}{delimiter}{extension}], length should of 2 ~ 4,"
						+ " for example: 1.10.0.2a or 1_10_0_2b or 1-10-0-2b etc, The delimiter should satisfy the version regex: '%s'",
						version, versionRegex));
		return verParts;
	}

	public static final String DEFAULT_VERSION_REGEX = "[-_./;:]";

}
