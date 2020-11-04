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
package com.wl4g.components.common.function;

import static com.wl4g.components.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.components.common.serialize.JacksonUtils.toJSONString;
import static java.lang.System.out;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.wl4g.components.common.function.TreeConvertor.TreeNode;

/**
 * {@link TreeConvertorTests}
 *
 * @author Wangl.sir <wanglsir@gmail.com, 983708408@qq.com>
 * @version v1.0 2017-09-08
 * @since
 */
public class TreeConvertorTests {

	public static void main(String[] args) {
		// 创建树结构转换器
		TreeConvertor<MyTree> convert = new TreeConvertor<>();

		// 将平面树转为children树
		List<MyTree> childrenTree = convert.formatToChildren(planeTree, false);
		String jsonStr = toJSONString(childrenTree);
		out.println("----------------");
		out.println(jsonStr);

		List<MyTree> someChildrenTree = parseJSON(jsonStr, new TypeReference<List<MyTree>>() {
		});
		// 获取父节点下所有子孙节点（包括本身）
		out.println("----------------");
		out.println(toJSONString(convert.subChildrens(someChildrenTree, "1")));

		// 将children树转为平面树
		out.println("----------------");
		out.println(toJSONString(convert.parseChildren(childrenTree)));
	}

	public static class MyTree implements TreeNode<MyTree> {
		private static final long serialVersionUID = 3429949759108637800L;

		private String id;
		private String name;
		private String parentId;
		private Integer level;
		private Integer sum;
		private Object data;
		private Double total;
		private List<MyTree> childrens;

		public MyTree() {
			super();
		}

		public MyTree(String id, String parentId, String name) {
			super();
			this.id = id;
			this.parentId = parentId;
			this.name = name;
		}

		public MyTree(String id, String parentId, String name, Double value) {
			super();
			this.id = id;
			this.parentId = parentId;
			this.name = name;
			this.data = value;
		}

		@Override
		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		@Override
		public String getParentId() {
			return parentId;
		}

		@Override
		public void setParentId(String parentId) {
			this.parentId = parentId;
		}

		public Integer getLevel() {
			return level;
		}

		public void setLevel(Integer level) {
			this.level = level;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public Object getData() {
			return data;
		}

		@Override
		public void setData(Object data) {
			this.data = data;
		}

		public Double getTotal() {
			return total;
		}

		public void setTotal(Double total) {
			this.total = total;
		}

		public void setChildrens(List<MyTree> children) {
			this.childrens = children;
		}

		public List<MyTree> getChildrens() {
			return childrens;
		}

		@Override
		public String toString() {
			return "MyTree [id=" + id + ", name=" + name + ", parentId=" + parentId + ", level=" + level + ", value=" + data
					+ ", total=" + total + ", childrens=" + childrens + "]";
		}

		@Override
		public Integer getSum() {
			return sum;
		}

		@Override
		public void setSum(Integer sum) {
			this.sum = sum;
		}

	}

	static List<MyTree> planeTree = new ArrayList<MyTree>();

	static {
		MyTree n0 = new MyTree("0", null, "顶点", 0.11d);
		MyTree n02 = new MyTree("02", null, "顶点2", 0.22d);
		MyTree n1 = new MyTree("1", "0", "节点1", 1.11d);
		MyTree n2 = new MyTree("2", "0", "节点2", 2.11d);
		MyTree n3 = new MyTree("3", "1", "节点3", 3.11d);
		MyTree n4 = new MyTree("4", "1", "节点4", 4.11d);
		MyTree n5 = new MyTree("5", "1", "节点5", 5.11d);
		MyTree n6 = new MyTree("6", "2", "节点6", 6.11d);
		MyTree n7 = new MyTree("7", "3", "节点7", 7.11d);
		planeTree.add(n2);
		planeTree.add(n0);
		planeTree.add(n1);
		planeTree.add(n02);
		planeTree.add(n3);
		planeTree.add(n4);
		planeTree.add(n5);
		planeTree.add(n6);
		planeTree.add(n7);
	}

}
