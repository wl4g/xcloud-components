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

import org.junit.Test;

/**
 * Make test files or directories: </br>
 * </br>
 * 
 * <pre>
 * mkdir -p /tmp/fileDeletion/dir1 && \
 * mkdir -p /tmp/fileDeletion/dir2 && \
 * mkdir -p /tmp/fileDeletion/dir2/dir21 && \
 * touch /tmp/fileDeletion/file.txt && \
 * touch /tmp/fileDeletion/dir1/file11.txt && \
 * touch /tmp/fileDeletion/dir2/file21.txt && \
 * touch /tmp/fileDeletion/dir2/dir21/file2211.txt && \
 * cd /tmp/fileDeletion/ && tree
 * </pre>
 * 
 * Show files tree graph:</br>
 * </br>
 * 
 * <pre>
 * .
 * ├── dir1
 * │   └── file11.txt
 * ├── dir2
 * │   ├── dir21
 * │   │   └── file2211.txt
 * │   └── file21.txt
 * └── file.txt
 * </pre>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version 2020-09-28
 * @sine v1.0.0
 * @see
 */
public class FileDeletionUtilsTests {

	/**
	 * Deleted files tree graph result:
	 * 
	 * <pre>
	 * .
	 * ├── dir1
	 * │   └── file11.txt
	 * ├── dir2
	 * │   ├── dir21
	 * │   │   └── file2211.txt
	 * └── file.txt
	 * </pre>
	 */
	@Test
	public void deleteWithPatternCase1() {
		System.out.println("deleteWithPatternCase1...");
		FileDeletionUtils.delete("/tmp/fileDeletion/dir2", false);
	}

	/**
	 * Deleted files tree graph result:
	 * 
	 * <pre>
	 * .
	 * ├── dir1
	 * │   └── file11.txt
	 * ├── dir2
	 * │   ├── dir21
	 * │   │   └── file2211.txt
	 * └── file.txt
	 * </pre>
	 */
	@Test
	public void deleteWithPatternCase2() {
		System.out.println("deleteWithPatternCase2...");
		FileDeletionUtils.delete("/tmp/fileDeletion/dir2/", false);
	}

	/**
	 * Deleted files tree graph result:
	 * 
	 * <pre>
	 * .
	 * ├── dir1
	 * │   └── file11.txt
	 * ├── dir2
	 * │   ├── dir21
	 * │   │   └── file2211.txt
	 * └── file.txt
	 * </pre>
	 */
	@Test
	public void deleteWithPatternCase3() {
		System.out.println("deleteWithPatternCase3...");
		FileDeletionUtils.delete("/tmp/fileDeletion/dir2/*", false);
	}

	/**
	 * Deleted files tree graph result:
	 * 
	 * <pre>
	 * .
	 * ├── dir1
	 * │   └── file11.txt
	 * ├── dir2
	 * │   ├── dir21
	 * └── file.txt
	 * </pre>
	 */
	@Test
	public void deleteWithPatternCase4() {
		System.out.println("deleteWithPatternCase4...");
		FileDeletionUtils.delete("/tmp/fileDeletion/dir2/**", false);
	}

}
