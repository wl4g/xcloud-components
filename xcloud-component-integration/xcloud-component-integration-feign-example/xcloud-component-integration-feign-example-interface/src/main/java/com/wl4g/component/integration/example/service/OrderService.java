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
package com.wl4g.component.integration.example.service;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.List;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.wl4g.component.integration.example.model.OrderInfo;
import com.wl4g.component.integration.feign.core.annotation.FeignConsumer;

/**
 * {@link OrderService}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2021-03-18
 * @sine v1.0
 * @see
 */
@FeignConsumer("${provider.serviceId.feign-example-service:feign-example-service}")
@RequestMapping("/order-service")
public interface OrderService {

	// Notes: Cannot be used @GetMapping, because feign convention does not
	// allow it.
	@RequestMapping(value = "/findOrderByUser", method = GET)
	List<OrderInfo> findOrderByUser(@RequestParam("userId") Long userId);

	@RequestMapping(value = "/createOrder", method = POST)
	void createOrder(@RequestBody OrderInfo order, @RequestParam("goodsId") Long goodsId);

}
