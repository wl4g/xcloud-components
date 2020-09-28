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
package com.wl4g.components.common.io;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.startsWith;
import static com.wl4g.components.common.lang.Assert2.hasTextOf;
import static com.wl4g.components.common.lang.Assert2.notNullOf;
import static com.wl4g.components.common.lang.Assert2.state;
import static java.io.File.separator;

import java.io.File;
import java.io.IOException;

import javax.validation.constraints.NotBlank;

import com.wl4g.components.common.matching.AntPathMatcher;

/**
 * {@link FileDeletionUtils}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年4月1日
 * @since
 */
public abstract class FileDeletionUtils {

	/**
	 * Delete files according to the file path matching ant pattern.
	 * 
	 * @param delPathAntPattern
	 *            Note: Is the standard ant matching pattern
	 * @throws IllegalStateException
	 *             When fast failure is enabled, delete failure throws an
	 *             exception.
	 */
	public static void delete(@NotBlank String delPathAntPattern) {
		delete(delPathAntPattern, false);
	}

	/**
	 * Delete files according to the file path matching ant pattern.
	 * 
	 * @param delPathAntPattern
	 *            Note: Is the standard ant matching pattern
	 * @param fastfail
	 * @throws IllegalStateException
	 *             When fast failure is enabled, delete failure throws an
	 *             exception.
	 */
	public static void delete(@NotBlank String delPathAntPattern, boolean fastfail) {
		hasTextOf(delPathAntPattern, "delPathAntPattern");

		// Find start directory path.
		String startPath = delPathAntPattern;
		int startIndex = delPathAntPattern.indexOf("*");
		if (startIndex > 0) {
			startPath = delPathAntPattern.substring(0, startIndex);
		}
		File file = new File(startPath);
		if (file.exists()) {
			try {
				doDeleteFileOrDirectories(delPathAntPattern, file, fastfail);
			} catch (IllegalStateException | IOException e) {
				throw new IllegalStateException(e);
			}
		}
	}

	/**
	 * Recursion delete sub files only or sub directories.
	 * 
	 * @param delPathAntPattern
	 * @param path
	 * @param fastfail
	 * @throws IllegalStateException
	 *             When fast failure is enabled, the deletion failure will throw
	 *             an exception and interrupt the execution immediately, which
	 *             may result in some files not being deleted.
	 * @throws IOException
	 */
	private static final void doDeleteFileOrDirectories(String delPathAntPattern, File path, boolean fastfail)
			throws IllegalStateException, IOException {
		notNullOf(path, "path");

		if (path.exists()) {
			if (path.isFile()) {
				if (matchPathChildren(delPathAntPattern, path)) {
					boolean result = path.delete();
					if (fastfail) {
						state(result, "Cannot to delete sub file '%s'", path);
					}
				}
			} else {
				File[] childrens = path.listFiles();
				if (nonNull(childrens)) {
					// Recursion deletion children files.
					for (File child : childrens) {
						doDeleteFileOrDirectories(delPathAntPattern, child, fastfail);
					}
					// Delete this directory.
					if (matchPathChildren(delPathAntPattern, path)) { // TODO?
						boolean result = path.delete();
						if (fastfail) {
							state(result, "Cannot to delete sub file '%s'", path);
						}
					}
				}
			}
		}
	}

	/**
	 * Match deleting of children path pattern.
	 * 
	 * @param delPathAntPattern
	 * @param path
	 * @return
	 * @throws IOException
	 */
	private static final boolean matchPathChildren(String delPathAntPattern, File path) throws IOException {
		String cpath = path.getCanonicalPath();
		return defaultPathMatcher.match(delPathAntPattern, cpath)
				// When no pattern direct match.
				|| (!defaultPathMatcher.isPattern(delPathAntPattern) && startsWith(cpath, delPathAntPattern));
	}

	/**
	 * Match deleting of this path pattern.
	 * 
	 * @param delPathAntPattern
	 * @param path
	 * @return
	 */
	// private static final boolean matchPathOfThis(String delPathAntPattern,
	// File path) {
	// try {
	// return defaultPathMatcher.isPattern(delPathAntPattern);
	// } catch (IOException e) {
	// throw new IllegalStateException(e);
	// }
	// }

	/**
	 * Default delete path matcher.
	 */
	private static final AntPathMatcher defaultPathMatcher = new AntPathMatcher(separator);

}