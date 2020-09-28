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
import static com.wl4g.components.common.lang.Assert2.hasTextOf;
import static com.wl4g.components.common.lang.Assert2.state;
import static java.io.File.separator;

import java.io.File;

import javax.validation.constraints.NotBlank;

/**
 * {@link FileDeletionUtils}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2020年4月1日
 * @since
 */
public abstract class FileDeletionUtils {

	/**
	 * Delete sub files only or sub directories.
	 * 
	 * @param fileOrParentDir
	 * @throws IllegalStateException
	 *             When fast failure is enabled, the deletion failure will throw
	 *             an exception and interrupt the execution immediately, which
	 *             may result in some files not being deleted.
	 */
	public static void deleteAny(@NotBlank String fileOrParentDir) {
		deleteAny(fileOrParentDir, false);
	}

	/**
	 * Delete sub files only or sub directories.
	 * 
	 * @param fileOrParentDir
	 * @param fastfail
	 * @throws IllegalStateException
	 *             When fast failure is enabled, the deletion failure will throw
	 *             an exception and interrupt the execution immediately, which
	 *             may result in some files not being deleted.
	 */
	public static void deleteAny(@NotBlank String fileOrParentDir, boolean fastfail) throws IllegalStateException {
		hasTextOf(fileOrParentDir, "fileOrParentDir");
		File file = new File(fileOrParentDir);
		if (file.exists()) {
			if (file.isFile()) {
				deleteFile(fileOrParentDir, fastfail);
			} else {
				deleteDir(fileOrParentDir, fastfail);
			}
		}
	}

	/**
	 * Delete file only.
	 * 
	 * @param filename
	 * @param fastfail
	 * @throws IllegalStateException
	 *             When fast failure is enabled, the deletion failure will throw
	 *             an exception and interrupt the execution immediately, which
	 *             may result in some files not being deleted.
	 */
	public static void deleteFile(@NotBlank String filename, boolean fastfail) throws IllegalStateException {
		hasTextOf(filename, "filename");
		File file = new File(filename);
		if (file.exists() && file.isFile()) {
			if (fastfail) {
				state(file.delete(), "Cannot to delete file '%s'", file);
			}
		}
	}

	/**
	 * Delete the specified directory and all its sub files.
	 * 
	 * @param parentDir
	 * @param fastfail
	 * @throws IllegalStateException
	 *             When fast failure is enabled, delete failure throws an
	 *             exception
	 */
	public static void deleteDir(@NotBlank String parentDir, boolean fastfail) throws IllegalStateException {
		hasTextOf(parentDir, "parentDir");
		parentDir = parentDir.endsWith(separator) ? parentDir.concat(separator) : parentDir;

		File file = new File(parentDir);
		if (file.exists() && file.isDirectory()) { // Ignore file
			// Deletion sub directories.
			File[] files = file.listFiles();
			if (nonNull(files)) {
				for (File f : files) {
					deleteAny(f.getAbsolutePath(), fastfail);
				}
			}
			if (fastfail) {
				state(file.delete(), "Cannot to delete directories sub file '%s'", file);
			}
		}
	}

}