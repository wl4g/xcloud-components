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
package com.wl4g.component.core.bean;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wl4g.component.common.log.SmartLogger;
import com.wl4g.component.core.utils.expression.SpelExpressions;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiModelProperty.AccessMode;
import io.swagger.annotations.ApiParam;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

import static com.wl4g.component.common.log.SmartLoggerFactory.getLogger;
import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;
import static java.lang.String.valueOf;
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
	private static final transient SmartLogger log = getLogger(BaseBean.class);

	/**
	 * Bean unqiue ID.</br>
	 */
	@ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
	@ApiParam(readOnly = true, hidden = true)
	@JsonIgnoreProperties(allowGetters = true, allowSetters = true)
	private Long id;

	/**
	 * Is enabled
	 */
	private Integer enable;

	/**
	 * For data permission, associated Organization (tree) code query
	 */
	private String organizationCode;

	/**
	 * Bean info remark desciprtion.
	 */
	private String remark;

	/**
	 * Bean info create user.
	 */
	@ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
	@ApiParam(readOnly = true, hidden = true)
	@JsonIgnoreProperties(allowGetters = true, allowSetters = false)
	private Long createBy;

	/**
	 * Bean info create date.
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
	@ApiParam(readOnly = true, hidden = true)
	@JsonIgnoreProperties(allowGetters = true, allowSetters = false)
	private Date createDate;

	/**
	 * Human creation date.</br>
	 * </br>
	 * Note: In order to be compatible with the different usages of the
	 * annotations of swagger 2.x and 3.x, the safest way is to add all possible
	 * ways that will work.
	 */
	@ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
	@ApiParam(readOnly = true, hidden = true)
	@JsonIgnoreProperties(allowGetters = true, allowSetters = false)
	private String humanCreateDate;

	/**
	 * Bean info update user.
	 */
	@ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
	@ApiParam(readOnly = true, hidden = true)
	@JsonIgnoreProperties(allowGetters = true, allowSetters = false)
	private Long updateBy;

	/**
	 * Bean info update date.
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
	@ApiParam(readOnly = true, hidden = true)
	@JsonIgnoreProperties(allowGetters = true, allowSetters = false)
	private Date updateDate;

	/**
	 * Human updation date.</br>
	 * </br>
	 * Note: In order to be compatible with the different usages of the
	 * annotations of swagger 2.x and 3.x, the safest way is to add all possible
	 * ways that will work.
	 */
	@ApiModelProperty(readOnly = true, accessMode = AccessMode.READ_ONLY)
	@ApiParam(readOnly = true, hidden = true)
	@JsonIgnoreProperties(allowGetters = true, allowSetters = false)
	private String humanUpdateDate;

	/**
	 * Logistic delete status. </br>
	 * </br>
	 * Note: In order to be compatible with the different usages of the
	 * annotations of swagger 2.x and 3.x, the safest way is to add all possible
	 * ways that will work.
	 */
	@JsonIgnore
	@ApiModelProperty(readOnly = true, hidden = true)
	@ApiParam(hidden = true, readOnly = true)
	@JsonIgnoreProperties(allowGetters = false, allowSetters = false)
	private Integer delFlag;

	/**
	 * Execute method before inserting, need to call manually
	 * 
	 * @return return current preparing insert generated id.
	 */
	public Long preInsert() {
		setCreateDate(new Date());
		setCreateBy(getCurrentPrincipal());
		setUpdateDate(getCreateDate());
		setUpdateBy(getCreateBy());
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
		setUpdateBy(UNKNOWN_USER_ID);
	}

	public BaseBean withId(Long id) {
		this.id = id;
		return this;
	}

	public BaseBean withEnable(Integer enable) {
		setEnable(enable);
		return this;
	}

	public BaseBean withOrganizationCode(String organizationCode) {
		setOrganizationCode(organizationCode);
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

	public BaseBean withDelFlag(Integer delFlag) {
		setDelFlag(delFlag);
		return this;
	}

	// --- Function's. ---

	@Override
	public String toString() {
		return getClass().getSimpleName().concat("<").concat(toJSONString(this)).concat(">");
	}

	/**
	 * Gets current authentication principal(ID/name).
	 * 
	 * @return
	 */
	private static final Long getCurrentPrincipal() {
		Throwable err = null;
		Object principal = null; // Principal(ID/name)

		// Frist get by request
		// try {
		// HttpServletRequest request = ((ServletRequestAttributes)
		// RequestContextHolder.currentRequestAttributes())
		// .getRequest();
		// curPrincipal = request.getRemoteUser();
		// } catch (Throwable e1) {
		// err = e1;
		// log.warn("Cannot get request authenticated principal. cause by: {}",
		// err.getMessage());
		// }

		// Fallback get by IAM
		if (isNull(principal)) {
			try {
				principal = spelExpr.resolve("#{T(IamSecurityHolder).getPrincipalInfo().getPrincipalId()}");
			} catch (Throwable e2) {
				err = e2;
				log.warn("Cannot fallback get IAM authenticated principal. cause by: {}", err.getMessage());
			}
		}

		// Fallback get by spring security
		// if (isNull(curPrincipal)) {
		// try {
		// // org.springframework.security.core.userdetails.UserDetails
		// curPrincipal = spelExpr
		// .resolve("#{T(SecurityContextHolder).getContext().getAuthentication().getPrincipal().getName()}",
		// null);
		// } catch (Throwable e3) {
		// err = e3;
		// curPrincipal = UNKNOWN_USER_ID;
		// log.warn(format("Cannot fallback get spring-security authenticated
		// principal, using fallback: '%s', cause by: {}",
		// curPrincipal), err.getMessage());
		// }
		// }

		return Long.parseLong(valueOf(principal));
	}

	/**
	 * Generic Status: enabled
	 */
	public static transient final int ENABLED = 1;

	/**
	 * Generic Status: disabled
	 */
	public static transient final int DISABLED = 0;

	/**
	 * Generic Status: normal (not deleted)
	 */
	public static transient final int DEL_FLAG_NORMAL = 0;

	/**
	 * Generic Status: deleted
	 */
	public static transient final int DEL_FLAG_DELETE = 1;

	/**
	 * Unknown user ID.
	 */
	public static transient final long UNKNOWN_USER_ID = -1;

	/*
	 * Default super administrator user name.
	 */
	public static transient final String DEFAULT_SUPER_USER = "root";

	/**
	 * {@link SpelExpressions}
	 */
	public static transient final SpelExpressions spelExpr = SpelExpressions.create();

}