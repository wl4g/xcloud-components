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
package com.wl4g.components.data.mybatis.mapper;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import com.wl4g.components.common.id.SnowflakeIdGenerator;
import com.wl4g.components.common.log.SmartLogger;
import com.wl4g.components.core.bean.BaseBean;

import static com.wl4g.components.common.log.SmartLoggerFactory.getLogger;
import static java.util.Objects.isNull;

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
 */
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class, RowBounds.class,
				ResultHandler.class }) })
public class GenericBeanMapperInterceptor implements Interceptor {

	protected final SmartLogger log = getLogger(getClass());

	@Override
	public Object intercept(Invocation invoc) throws Throwable {
		if (isNull(invoc.getArgs())) {
			return invoc.proceed();
		}

		MappedStatement statement = (MappedStatement) invoc.getArgs()[0];
		SqlCommandType command = statement.getSqlCommandType();

		// Sets update|insert properties
		applyUpdationPropertiesIfNecessary(invoc, command);

		// Sets query properties
		applyQueryPropertiesIfNecessary(invoc, command);

		return invoc.proceed();
	}

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

	/**
	 * Sets properties attributes with updation and insertion(if necessary).
	 * 
	 * @param invoc
	 * @param command
	 */
	private void applyUpdationPropertiesIfNecessary(Invocation invoc, SqlCommandType command) {
		// Sets insert attributes
		if (command == SqlCommandType.INSERT) {
			for (int i = 1; i < invoc.getArgs().length; i++) {
				Object arg = invoc.getArgs()[i];
				if (BaseBean.class.isAssignableFrom(arg.getClass())) {
					BaseBean bean = (BaseBean) arg;
					// Assign sets primary key ID.
					if (!isNull(bean) && isNull(bean.getId())) {
						// TODO using by remote global IdGenerator servers.
						bean.setId(SnowflakeIdGenerator.getDefault().nextId());
						log.debug("Dynamic assigned primary key ID for: {}, method: {}", bean.getId(), invoc.getMethod());
					}
					if (isInsertSettable(bean)) {
						bean.preInsert();
					}
				}
			}
		}
		// Sets update attributes
		else if (command == SqlCommandType.UPDATE) {
			for (int i = 1; i < invoc.getArgs().length; i++) {
				Object arg = invoc.getArgs()[i];
				if (BaseBean.class.isAssignableFrom(arg.getClass())) {
					BaseBean bean = (BaseBean) arg;
					if (isUpdateSettable(bean)) {
						bean.preUpdate();
					}
				}
			}
		}

	}

	/**
	 * Sets properties with query(if necessary).
	 * 
	 * @param invoc
	 * @param command
	 */
	private void applyQueryPropertiesIfNecessary(Invocation invoc, SqlCommandType command) {
		/*
		 * Note: In order to be compatible with the distributed microservice
		 * architecture, it is not the best solution to convert the bean time
		 * returned by Dao layer here. Because Dao layer may not be able to
		 * obtain the I18N language of the current user, it is finally decided
		 * to migrate to the web layer.
		 */

		// Sets query result attributes
		if (command == SqlCommandType.SELECT) {
			// for (int i = 1; i < invoc.getArgs().length; i++) {
			// Object arg = invoc.getArgs()[i];
			// if (BaseBean.class.isAssignableFrom(arg.getClass())) {
			// BaseBean bean = (BaseBean) arg;
			// if (!isNull(bean) && (isNull(bean.getHumanCreateDate()) ||
			// isNull(bean.getHumanCreateDate()))) {
			// bean.setHumanCreateDate(isNull(bean.getCreateDate()) ? null
			// :
			// periodFormatter.formatHumanDate(bean.getCreateDate().getTime()));
			// bean.setHumanUpdateDate(isNull(bean.getUpdateDate()) ? null
			// :
			// periodFormatter.formatHumanDate(bean.getUpdateDate().getTime()));
			// }
			// }
			// }
		}

	}

	/**
	 * Check whether parameter properties need to be set when insertion.
	 * 
	 * @param bean
	 * @return
	 */
	private boolean isInsertSettable(BaseBean bean) {
		return isNull(bean.getCreateDate()) || isNull(bean.getCreateBy());
	}

	/**
	 * Check whether parameter properties need to be set when updating.
	 * 
	 * @param bean
	 * @return
	 */
	private boolean isUpdateSettable(BaseBean bean) {
		return isNull(bean.getUpdateDate()) || isNull(bean.getUpdateBy());
	}

	// /*
	// * Human date formatter instance.
	// */
	// private static final PeriodFormatter periodFormatter =
	// PeriodFormatter.getDefault().ignoreLowerDate(true);

}
