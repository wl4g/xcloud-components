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
package com.wl4g.component.common.io;

import org.apache.commons.compress.archivers.ArchiveException;

import java.io.IOException;

public class CompressUtilsTests {

	public static void main(String[] args) throws IOException, ArchiveException {
//		String srcPath = "/Users/vjay/Downloads/";
//		String fileName1 = "problem.txt";
//		String pathInTar1 = "hello2/";
//		String fileName2 = "sharding_sql.sql";
//		String pathInTar2 = "ok2/";
//		String tarName = "testtar.tar";

		// 构建TarArchiveOutputStream
		//TarArchiveOutputStream tout = CompressUtils.createTarArchiveOutputStream(srcPath+tarName);

		// 写第一个文件
		//CompressUtils.appendToTar(tout,srcPath,pathInTar1,fileName1);
		// 写第二个文件
		//CompressUtils.appendToTar(tout,srcPath,pathInTar2,fileName2);

		// 最后关闭TarArchiveOutputStream，tar包就形成了
		//tout.close();

		// gzip压缩
		//CompressUtils.gzip(srcPath+tarName);


		CompressUtils.appendToTar("/Users/vjay/Downloads/base-view-master-bin.tar","/Users/vjay/Downloads/rap1.json", "rap1.json");


	}

}