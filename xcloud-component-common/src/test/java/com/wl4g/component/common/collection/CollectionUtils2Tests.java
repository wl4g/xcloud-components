/*
 * Copyright (C) 2017 ~ 2025 the original author or authors.
 * <Wanglsir@gmail.com, 983708408@qq.com> Technology CO.LTD.
 * All rights reserved.
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
 * 
 * Reference to website: http://wl4g.com
 */
package com.wl4g.component.common.collection;

import static com.wl4g.component.common.collection.CollectionUtils2.extractElement;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link CollectionUtils2Tests}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-01-12
 * @sine v1.0
 * @see
 */
public class CollectionUtils2Tests {

	public static void main(String[] args) {
		List<String> list = new ArrayList<>();
		list.add("Trump");
		list.add("Biden");
		list.add("Pelosi-West");
		list.add("Obama-West");
		System.out.println(extractElement(list, 0, null));
	}

}
