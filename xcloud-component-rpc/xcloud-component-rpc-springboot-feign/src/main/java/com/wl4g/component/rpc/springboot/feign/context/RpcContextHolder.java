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
package com.wl4g.component.rpc.springboot.feign.context;

import static com.wl4g.component.common.collection.CollectionUtils2.safeMap;
import static com.wl4g.component.common.lang.Assert2.hasTextOf;
import static com.wl4g.component.common.lang.Assert2.notNullOf;
import static com.wl4g.component.common.reflect.TypeUtils2.isSimpleType;
import static com.wl4g.component.common.serialize.JacksonUtils.parseJSON;
import static com.wl4g.component.common.serialize.JacksonUtils.toJSONString;
import com.wl4g.component.core.utils.context.SpringContextHolder;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.apache.commons.beanutils.ConvertUtilsBean;

/**
 * {@link RpcContextHolder}
 * 
 * @author Wangl.sir &lt;wanglsir@gmail.com, 983708408@qq.com&gt;
 * @version v1.0 2020-12-19
 * @sine v1.0
 * @see
 */
public abstract class RpcContextHolder {

	/** Singleton holder instance */
	private static volatile RpcContextHolder provider;

	/** References attachments repository implementation. */
	private final ReferenceRepository repository;

	protected RpcContextHolder() {
		this(ReferenceRepository.NOOP);
	}

	protected RpcContextHolder(ReferenceRepository repository) {
		this.repository = notNullOf(repository, "referenceRepository");
	}

	/**
	 * Obtain singleton instance of {@link RpcContextHolder}
	 * 
	 * @return
	 */
	public static final RpcContextHolder get() {
		if (isNull(provider)) {
			synchronized (RpcContextHolder.class) {
				if (isNull(provider)) {
					provider = initAvailableHolderProvider();
				}
			}
		}
		return provider.current();
	}

	/**
	 * Gets or create current this instance.
	 * 
	 * @return
	 */
	protected abstract RpcContextHolder current();

	/**
	 * Gets attribute from original Rpc context. {@link #set(String, Object)}
	 * 
	 * @param key
	 * @param valueType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <T> T get(@NotBlank String key, @NotNull Class<T> valueType) {
		hasTextOf(key, "attachmentKey");
		notNullOf(valueType, "attachmentValueType");
		Object value = getAttachment(key);
		if (isNull(value)) {
			return null;
		} else if (String.class.isAssignableFrom(valueType)) {
			return (T) value;
		} else if (isSimpleType(valueType)) {
			return (T) defaultConverter.convert(value, valueType);
		} else { // Custom object
			return parseJSON((String) value, valueType);
		}
	}

	/**
	 * Sets attribute to original Rpc context. {@link #set(String, Object)}
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public void set(@NotBlank String key, @Nullable Object value) {
		hasTextOf(key, "attachmentKey");
		if (!isNull(value)) {
			if (value instanceof String) {
				setAttachment(key, (String) value);
			} else if (isSimpleType(value.getClass())) {
				setAttachment(key, defaultConverter.convert(value));
			} else { // Other object
				setAttachment(key, toJSONString(value));
			}
		}
	}

	/**
	 * Gets attachment from current rpc context.
	 * 
	 * @return
	 */
	public abstract String getAttachment(String key);

	/**
	 * Gets all attachments from current rpc context.
	 * 
	 * @return
	 */
	public abstract Map<String, String> getAttachments();

	/**
	 * Sets attachment to current rpc context.
	 * 
	 * @param key
	 * @param value
	 */
	public abstract void setAttachment(String key, String value);

	/**
	 * Sets all attachments to current rpc context.
	 * 
	 * @param key
	 * @param value
	 */
	public void setAttachments(Map<? extends String, ? extends String> attachments) {
		safeMap(attachments).forEach((key, value) -> setAttachment(key, value));
	}

	/**
	 * Remove current context attachment element.
	 * 
	 * @param key
	 */
	public abstract void removeAttachment(String key);

	/**
	 * Remove all attachments to current rpc context.
	 */
	public abstract void clearAttachments();

	/**
	 * Gets current remote port.
	 * 
	 * @return
	 */
	public Integer getRemotePort() {
		return get("remotePort", Integer.class);
	}

	/**
	 * Gets current remote host.
	 * 
	 * @return
	 */
	public String getRemoteHost() {
		return get("remoteHost", String.class);
	}

	/**
	 * Gets current rpc local port
	 * 
	 * @return
	 */
	public Integer getLocalPort() {
		return get("localPort", Integer.class);
	}

	/**
	 * Gets current rpc local host.
	 * 
	 * @return
	 */
	public String getLocalHost() {
		return get("localHost", String.class);
	}

	//
	// --- References attachemnts implementation. ---
	//

	public <T> T get(@NotBlank ReferenceKey key, @NotNull Class<T> valueType) {
		notNullOf(key, "referenceKey");
		return repository.doGetReferenceValue(key.getKey(), valueType);
	}

	public void set(@NotBlank ReferenceKey key, @Nullable Object value) {
		notNullOf(key, "referenceKey");
		set(key.getKey(), value);
		repository.doSetReferenceValue(key.getKey(), value);
	}

	/**
	 * Initilization obtain available candidate implements
	 * {@link RpcContextHolder} singleton instance.
	 * 
	 * @return
	 */
	private static final RpcContextHolder initAvailableHolderProvider() {
		List<RpcContextHolder> candidateHolders = safeMap(SpringContextHolder.getBeans(RpcContextHolder.class)).values().stream()
				.collect(toList());

		// Check holders must have only one valid.
		if (candidateHolders.isEmpty()) {
			throw new Error(format("Error, shouldn't be here, No found %s instance.", RpcContextHolder.class.getSimpleName()));
		} else if (candidateHolders.size() > 1) {
			throw new IllegalStateException(format(
					"Found multiple %s instances, multiple RPC frameworks coexistence (e.g. feign/dubbo/motan) are not supported. Please check the conflicting framework dependencies.",
					RpcContextHolder.class.getSimpleName()));
		} else {
			return candidateHolders.get(0);
		}
	}

	/**
	 * Reference type attachment attribute key. Using this type of attachment
	 * key, only the referenced key will be passed in the RPC context, and the
	 * actual value object will not be passed directly. Usage scenario: it is
	 * very useful for attachments with large object value and low frequency of
	 * use, which can greatly improve performance.
	 */
	public final static class ReferenceKey {
		private final String key;

		public ReferenceKey(String key) {
			super();
			this.key = key;
		}

		public String getKey() {
			return key;
		}
	}

	/**
	 * Refer to {@link ReferenceKey}
	 */
	public static interface ReferenceRepository {
		default <T> T doGetReferenceValue(@NotBlank String referenceKey, @NotNull Class<T> valueType) {
			// throw new UnsupportedOperationException(format("Not
			// implementation of %s", getClass()));
			return null;
		}

		default void doSetReferenceValue(@NotBlank String referenceKey, @Nullable Object value) {
			// throw new UnsupportedOperationException(format("Not
			// implementation of %s", getClass()));
		}

		public static final ReferenceRepository NOOP = new ReferenceRepository() {
		};
	}

	/** Default types converter. */
	private static final ConvertUtilsBean defaultConverter = new ConvertUtilsBean();

}
