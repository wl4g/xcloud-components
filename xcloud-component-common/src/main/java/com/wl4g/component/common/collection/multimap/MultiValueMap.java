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
package com.wl4g.component.common.collection.multimap;

import java.util.List;
import java.util.Map;

/**
 * Extension of the {@code Map} interface that stores multiple values.
 *
 * @param <K>
 *            the key type
 * @param <V>
 *            the value element type
 */
public interface MultiValueMap<K, V> extends Map<K, List<V>> {

	/**
	 * Return the first value for the given key.
	 * 
	 * @param key
	 *            the key
	 * @return the first value for the specified key, or {@code null} if none
	 */
	V getFirst(K key);

	/**
	 * Add the given single value to the current list of values for the given
	 * key.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value to be added
	 */
	void add(K key, V value);

	/**
	 * Add all the values of the given list to the current list of values for
	 * the given key.
	 * 
	 * @param key
	 *            they key
	 * @param values
	 *            the values to be added
	 * @since 5.0
	 */
	void addAll(K key, List<? extends V> values);

	/**
	 * Add all the values of the given {@code MultiValueMap} to the current
	 * values.
	 * 
	 * @param values
	 *            the values to be added
	 * @since 5.0
	 */
	void addAll(MultiValueMap<K, V> values);

	/**
	 * {@link #add(Object, Object) Add} the given value, only when the map does
	 * not {@link #containsKey(Object) contain} the given key.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value to be added
	 * @since 5.2
	 */
	default void addIfAbsent(K key, V value) {
		if (!containsKey(key)) {
			add(key, value);
		}
	}

	/**
	 * Set the given single value under the given key.
	 * 
	 * @param key
	 *            the key
	 * @param value
	 *            the value to set
	 */
	void set(K key, V value);

	/**
	 * Set the given values under.
	 * 
	 * @param values
	 *            the values.
	 */
	void setAll(Map<K, V> values);

	/**
	 * Return a {@code Map} with the first values contained in this
	 * {@code MultiValueMap}.
	 * 
	 * @return a single value representation of this map
	 */
	Map<K, V> toSingleValueMap();

}