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
package com.wl4g.component.data.mybatis.mapper;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.SqlUtil;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import static org.apache.ibatis.mapping.SqlCommandType.*;

import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.core.bean.BaseBean;
import com.wl4g.component.core.bean.model.PageHolder;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import java.util.Properties;

/**
 * General {@link BaseBean} property functions handle interceptors in a unified
 * way, for example, generating IDs for insertion methods (supports local or
 * remote generators)</br>
 * </br>
 * 
 * <b>In mybatis, there are four types that can be intercepted (in the order of
 * interception type):</b></br>
 * </br>
 * Executor：Method of intercepting actuator</br>
 * ParameterHandler：Processing of interception parameters</br>
 * ResultHandler：Processing of interception result set</br>
 * StatementHandler：Processing of intercepting SQL syntax construction</br>
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-11-17
 * @sine v1.0
 * @see https://www.jianshu.com/p/0a72bb1f6a21
 * @see https://cloud.tencent.com/developer/article/1170370
 */
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class,
				ResultHandler.class }) })
public class PreparedBeanMapperInterceptor implements Interceptor {

	protected final SmartLogger log = getLogger(getClass());

	@Autowired
	private IdGenerator idGenerator;

	/**
	 * Intercepting executor only
	 *
	 * @param target
	 * @return
	 */
	@Override
	public Object plugin(Object target) {
		if (target instanceof Executor) {
			return Plugin.wrap(target, this);
		} else {
			return target;
		}
	}

	@Override
	public void setProperties(Properties properties) {
	}

	@Override
	public Object intercept(Invocation invoc) throws Throwable {
		if (isNull(invoc.getArgs())) {
			return invoc.proceed();
		}
		MappedStatement statement = (MappedStatement) invoc.getArgs()[0];
		SqlCommandType command = statement.getSqlCommandType();

		// Prepared properties.
		preProcess(invoc, command);

		// Invoke mapper process
		final Object result = invoc.proceed();

		// Post properties.
		postProcess(invoc, command, result);

		return result;
	}

	/**
	 * Prepared process.
	 * 
	 * @param invoc
	 * @param command
	 */
	private void preProcess(Invocation invoc, SqlCommandType command) {
		if (command == SELECT) {
			preQueryPropertiesSet(invoc);
		} else if (command == UPDATE) {
			preUpdatePropertiesSet(invoc);
		} else if (command == INSERT) {
			preInsertPropertiesSet(invoc);
		}
	}

	/**
	 * Post process.
	 * 
	 * @param invoc
	 * @param command
	 * @param result
	 */
	private void postProcess(Invocation invoc, SqlCommandType command, Object result) {
		if (command == SELECT) {
			postQueryPropertiesSet(invoc, result);
		} else if (command == UPDATE) {
			postUpdatePropertiesSet(invoc, result);
		} else if (command == INSERT) {
			postInsertPropertiesSet(invoc, result);
		}
	}

	/**
	 * Pre query properties set. (if necessary)
	 * 
	 * @param invoc
	 * @param command
	 */
	protected void preQueryPropertiesSet(Invocation invoc) {
		/*
		 * Note: In order to be compatible with the distributed microservice
		 * architecture, it is not the best solution to convert the bean time
		 * returned by Dao layer here. Because Dao layer may not be able to
		 * obtain the I18N language of the current user, it is finally decided
		 * to migrate to the web layer.
		 */
		if (isNull(SqlUtil.getLocalPage())) { // No set?
			// Obtain page from Rpc context.
			PageHolder<?> page = PageHolder.getCurrentPage();
			log.debug("Start current pagination of RpcContext holder. -> {}", page);
			if (nonNull(page)) {
				Page<Object> helperPage = PageHelper.startPage(page.getPageNum(), page.getPageSize(), page.getPage().isCount());
				helperPageLocal.set(helperPage);
			}
		}
	}

	/**
	 * Pre updation properties set.(if necessary)
	 * 
	 * @param invoc
	 * @param command
	 */
	protected void preUpdatePropertiesSet(Invocation invoc) {
		for (int i = 1; i < invoc.getArgs().length; i++) {
			Object arg = invoc.getArgs()[i];
			if (BaseBean.class.isAssignableFrom(arg.getClass())) {
				BaseBean bean = (BaseBean) arg;
				if (isUpdateSettable(bean)) {
					bean.preUpdate();
				}
				break;
			}
		}
	}

	/**
	 * Pre insertion properties set.(if necessary)
	 * 
	 * @param invoc
	 */
	protected void preInsertPropertiesSet(Invocation invoc) {
		// Sets insert properties
		for (int i = 1; i < invoc.getArgs().length; i++) {
			Object arg = invoc.getArgs()[i];
			if (BaseBean.class.isAssignableFrom(arg.getClass())) {
				BaseBean bean = (BaseBean) arg;
				// Assign sets primary key ID.
				if (!isNull(bean) && isNull(bean.getId())) {
					bean.setId(idGenerator.nextId(bean));
					log.debug("Dynamic assigned primary key ID for: {}, method: {}", bean.getId(), invoc.getMethod());
				}
				if (isInsertSettable(bean)) {
					bean.preInsert();
				}
				break;
			}
		}
	}

	/**
	 * Post query properties set. (if necessary)
	 * 
	 * @param invoc
	 * @param result
	 */
	protected void postQueryPropertiesSet(Invocation invoc, Object result) {
		PageHolder<?> page = PageHolder.getCurrentPage();
		if (nonNull(page)) {
			Page<?> helperPage = helperPageLocal.get();
			if (nonNull(helperPage)) {
				try {
					BeanUtils.copyProperties(helperPage, page.getPage());
					page.setPages(100112123);
					PageHolder.setCurrentPage(page);
				} finally {
					helperPageLocal.remove();
				}
			}
		}
	}

	/**
	 * Post updation properties set.(if necessary)
	 * 
	 * @param invoc
	 * @param result
	 */
	protected void postUpdatePropertiesSet(Invocation invoc, Object result) {
	}

	/**
	 * Post insertion properties set.(if necessary)
	 * 
	 * @param invoc
	 * @param result
	 */
	protected void postInsertPropertiesSet(Invocation invoc, Object result) {
	}

	/**
	 * Check whether parameter properties need to be set when insertion.
	 * 
	 * @param bean
	 * @return
	 */
	protected boolean isInsertSettable(BaseBean bean) {
		return isNull(bean.getCreateDate()) || isNull(bean.getCreateBy());
	}

	/**
	 * Check whether parameter properties need to be set when updating.
	 * 
	 * @param bean
	 * @return
	 */
	protected boolean isUpdateSettable(BaseBean bean) {
		return isNull(bean.getUpdateDate()) || isNull(bean.getUpdateBy());
	}

	/** Helper {@link Page} copy cache, for binding rpc context. */
	private static final ThreadLocal<Page<?>> helperPageLocal = new ThreadLocal<>();

}
