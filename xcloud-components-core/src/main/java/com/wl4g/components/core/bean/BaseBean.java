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
package com.wl4g.components.core.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.wl4g.components.common.id.SnowflakeIdGenerator;
import com.wl4g.components.common.lang.period.PeriodFormatter;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

import static com.wl4g.components.common.serialize.JacksonUtils.toJSONString;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isBlank;

/**
 * DB based bean entity.
 *
 * @author Wangl.sir &lt;Wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0.0 2018-09-05
 * @since
 */
@Getter
@Setter
public abstract class BaseBean implements Serializable {

	private static final long serialVersionUID = 8940373806493080114L;

	/**
	 * Bean unqiue ID.</br>
	 * </br>
	 * 
	 * Note: Fix the precision problem of JS long: since JavaScript follows IEEE
	 * 754 specification, the value of Java long type is beyond its processing
	 * scope, so it is necessary to serialize the long type field to string
	 * type.
	 * 
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	/**
	 * Bean info create user.
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long createBy;

	/**
	 * Bean info create date.
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date createDate;

	/**
	 * Bean info update user.
	 */
	@JsonSerialize(using = ToStringSerializer.class)
	private Long updateBy;

	/**
	 * Bean info update date.
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Date updateDate;

	/**
	 * Is enabled
	 */
	private Integer enable;

	/**
	 * Bean info remark desciprtion.
	 */
	private String remark;

	/**
	 * For data permission, associated Organization (tree) code query
	 */
	private String organizationCode;

	/**
	 * Logistic delete status.
	 */
	private Integer delFlag;

	/**
	 * Execute method before inserting, need to call manually
	 * 
	 * @return return current preparing insert generated id.
	 */
	public Long preInsert() {
		// TODO
		// This is a temporary ID generation scheme. You can change
		// it to a primary key generation service later.
		setId(SnowflakeIdGenerator.getDefault().nextId());

		setCreateDate(new Date());
		setCreateBy(DEFAULT_USER_ID);
		setUpdateDate(getCreateDate());
		setUpdateBy(DEFAULT_USER_ID);
		setDelFlag(DEL_FLAG_NORMAL);
		setEnable(ENABLED);

		return getId();
	}

	/**
	 * Execute method before inserting, need to call manually
	 *
	 * @param organizationCode
	 * @return return current preparing insert generated id.
	 */
	public Long preInsert(String organizationCode) {
		if (isBlank(getOrganizationCode())) {
			setOrganizationCode(organizationCode);
		}
		return preInsert();
	}

	/**
	 * Execute method before update, need to call manually
	 */
	public void preUpdate() {
		setUpdateDate(new Date());
		setUpdateBy(DEFAULT_USER_ID);
	}

	public BaseBean withId(Long id) {
		this.id = id;
		return this;
	}

	public BaseBean withEnable(Integer enable) {
		setEnable(enable);
		return this;
	}

	public BaseBean withRemark(String remark) {
		setRemark(remark);
		return this;
	}

	public BaseBean withCreateBy(Long createBy) {
		setCreateBy(createBy);
		return this;
	}

	public BaseBean withCreateDate(Date createDate) {
		setCreateDate(createDate);
		return this;
	}

	public BaseBean withUpdateBy(Long updateBy) {
		setUpdateBy(updateBy);
		return this;
	}

	public BaseBean withUpdateDate(Date updateDate) {
		setUpdateDate(updateDate);
		return this;
	}

	public BaseBean withOrganizationCode(String organizationCode) {
		setOrganizationCode(organizationCode);
		return this;
	}

	public BaseBean withDelFlag(Integer delFlag) {
		setDelFlag(delFlag);
		return this;
	}

	// --- Function's. ---

	public String getHumanCreateDate() {
		return isNull(getCreateDate()) ? null : defaultPeriodFormatter.formatHumanDate(getCreateDate().getTime());
	}

	public String getHumanUpdateDate() {
		return isNull(getUpdateDate()) ? null : defaultPeriodFormatter.formatHumanDate(getUpdateDate().getTime());
	}

	@Override
	public String toString() {
		return getClass().getSimpleName().concat("<").concat(toJSONString(this)).concat(">");
	}

	/**
	 * Generic Status: enabled
	 */
	public static final int ENABLED = 1;

	/**
	 * Generic Status: disabled
	 */
	public static final int DISABLED = 0;

	/**
	 * Generic Status: normal (not deleted)
	 */
	public static final int DEL_FLAG_NORMAL = 0;

	/**
	 * Generic Status: deleted
	 */
	public static final int DEL_FLAG_DELETE = 1;

	/**
	 * Default userId.
	 */
	public static final long DEFAULT_USER_ID = 1;

	/*
	 * Default userName: Super administrator account.
	 */
	public static final String DEFAULT_USER_ROOT = "root";

	/*
	 * Human date formatter instance.
	 */
	public static final PeriodFormatter defaultPeriodFormatter = PeriodFormatter.getDefault().ignoreLowerDate(true);

}