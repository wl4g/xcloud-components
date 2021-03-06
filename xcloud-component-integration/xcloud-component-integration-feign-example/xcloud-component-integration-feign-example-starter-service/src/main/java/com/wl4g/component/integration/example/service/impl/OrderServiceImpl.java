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
package com.wl4g.component.integration.example.service.impl;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.core.page.PageHolder;
import com.wl4g.component.integration.example.bean.OrderInfo;
import com.wl4g.component.integration.example.dao.OrderDao;
import com.wl4g.component.integration.example.service.OrderService;

/**
 * {@link OrderServiceImpl}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-03-18
 * @sine v1.0
 * @see
 */
@Service
public class OrderServiceImpl implements OrderService {

	private final SmartLogger log = getLogger(getClass());

	private @Autowired OrderDao orderDao;

	@Override
	public List<OrderInfo> list(PageHolder<OrderInfo> page, String orderName) {
		page.useCount().bind();
		List<OrderInfo> orders = orderDao.list(orderName);
		log.info("find orders result: {}", orders);
		return orders;
	}

	@Override
	public int createOrder(OrderInfo order, Long goodsId) {
		log.info("create order: {}, goodsId: {}", order, goodsId);
		return orderDao.insertSelective(order);
	}

}
