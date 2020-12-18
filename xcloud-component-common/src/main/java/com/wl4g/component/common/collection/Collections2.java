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
package com.wl4g.component.common.collection;

import static com.wl4g.component.common.collection.CollectionUtils2.isEmpty;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Objects.isNull;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.EnumerationUtils;

/**
 * Collection2 utility.
 * 
 * @author Wangl.sir
 * @version v1.0 2019年7月26日
 * @since
 */
public abstract class Collections2 {

	/**
	 * Is empty array.
	 * 
	 * @param collection
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> boolean isEmptyArray(T... array) {
		return null == array || array.length <= 0;
	}

	/**
	 * Safe collection list.
	 * 
	 * @param collection
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> T[] safeArray(Class<T> componentType, T... array) {
		return null == array ? (T[]) Array.newInstance(componentType, 0) : array;
	}

	/**
	 * Ensure that the default is at least an ArrayList instance (when the
	 * parameter is empty)
	 * 
	 * @param list
	 * @return
	 */
	public static <T> List<T> safeArrayToList(T[] array) {
		return isNull(array) ? emptyList() : asList(array);
	}

	/**
	 * Ensure that the default is at least an ArrayList instance (when the
	 * parameter is empty)
	 * 
	 * @param list
	 * @return
	 */
	public static <T> List<T> ensureList(List<T> list) {
		return isEmpty(list) ? new ArrayList<T>() : list;
	}

	/**
	 * Ensure that the default is at least an fallback list instance (when the
	 * parameter is empty)
	 * 
	 * @param list
	 * @param fallback
	 * @return
	 */
	public static <T> List<T> ensureList(List<T> list, List<T> fallback) {
		return isEmpty(list) ? fallback : list;
	}

	/**
	 * Ensure that the default is at least an HashSet instance (when the
	 * parameter is empty)
	 * 
	 * @param set
	 * @return
	 */
	public static <T> Set<T> ensureSet(Set<T> set) {
		return isEmpty(set) ? new HashSet<T>() : set;
	}

	/**
	 * Ensure that the default is at least an fallback set instance (when the
	 * parameter is empty)
	 * 
	 * @param set
	 * @param fallback
	 * @return
	 */
	public static <T> Set<T> ensureSet(Set<T> set, Set<T> fallback) {
		return isEmpty(set) ? fallback : set;
	}

	/**
	 * Ensure that the default is at least an HashMap instance (when the
	 * parameter is empty)
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V> Map<K, V> ensureMap(Map<K, V> map) {
		return isEmpty(map) ? new HashMap<>() : map;
	}

	/**
	 * Ensure that the default is at least an fallback map instance (when the
	 * parameter is empty)
	 * 
	 * @param map
	 * @param fallback
	 * @return
	 */
	public static <K, V> Map<K, V> ensureMap(Map<K, V> map, Map<K, V> fallback) {
		return isEmpty(map) ? fallback : map;
	}

	/**
	 * Safe enumeration to list.
	 * 
	 * @param enum
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> safeEnumerationToList(Enumeration<T> enumeration) {
		return isNull(enumeration) ? emptyList() : EnumerationUtils.toList(enumeration);
	}

	/**
	 * Safe collection list.
	 * 
	 * @param list
	 * @return
	 */
	public static <T> List<T> safeList(List<T> list) {
		return isEmpty(list) ? emptyList() : list;
	}

	/**
	 * Safe array to list.
	 * 
	 * @param array
	 * @return
	 */
	public static <T> List<T> safeToList(Class<T> componentType, T[] array) {
		return Arrays.asList(safeArray(componentType, array));
	}

	/**
	 * Safe collection set.
	 * 
	 * @param set
	 * @return
	 */
	public static <T> Set<T> safeSet(Set<T> set) {
		return isEmpty(set) ? emptySet() : set;
	}

	/**
	 * Safe collection map.
	 * 
	 * @param map
	 * @return
	 */
	public static <K, V> Map<K, V> safeMap(Map<K, V> map) {
		return CollectionUtils2.isEmpty(map) ? emptyMap() : map;
	}

	/**
	 * Remove duplicate collection elements.
	 * 
	 * @param list
	 * @return
	 */
	public static <T> Collection<T> disDupCollection(Collection<T> list) {
		Set<T> disDupSet = new HashSet<>(list);
		list.clear();
		list.addAll(disDupSet);
		return list;
	}

}