/*
 * Copyright 2002-2014 the original author or authors.
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

package org.springframework.beans.factory.config;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.springframework.beans.factory.FactoryBean;

/**
 * Factory for Map that reads from a YAML source. YAML is a nice human-readable
 * format for configuration, and it has some useful hierarchical properties. It's
 * more or less a superset of JSON, so it has a lot of similar features. If
 * multiple resources are provided the later ones will override entries in the
 * earlier ones hierarchically - that is all entries with the same nested key of
 * type Map at any depth are merged. For example:
 *
 * <pre class="code">
 * foo:
 *   bar:
 *    one: two
 * three: four
 *
 * </pre>
 *
 * plus (later in the list)
 *
 * <pre class="code">
 * foo:
 *   bar:
 *    one: 2
 * five: six
 *
 * </pre>
 *
 * results in an effective input of
 *
 * <pre class="code">
 * foo:
 *   bar:
 *    one: 2
 * three: four
 * five: six
 *
 * </pre>
 *
 * Note that the value of "foo" in the first document is not simply replaced
 * with the value in the second, but its nested values are merged.
 *
 * @author Dave Syer
 * @since 4.1
 */
public class YamlMapFactoryBean extends YamlProcessor implements
		FactoryBean<Map<String, Object>> {

	private boolean singleton = true;

	private Map<String, Object> singletonInstance;


	/**
	 * Set whether a shared 'singleton' Map instance should be
	 * created, or rather a new Map instance on each request.
	 * <p>Default is "true" (a shared singleton).
	 */
	public final void setSingleton(boolean singleton) {
		this.singleton = singleton;
	}

	@Override
	public final boolean isSingleton() {
		return this.singleton;
	}


	@Override
	public Map<String, Object> getObject() {
		if (!this.singleton || this.singletonInstance == null) {
			this.singletonInstance = createProperties();
		}
		return this.singletonInstance;
	}

	@Override
	public Class<?> getObjectType() {
		return Map.class;
	}

	/**
	 * Template method that subclasses may override to construct the object
	 * returned by this factory. The default implementation returns the
	 * merged Map instance.
	 * <p>Invoked lazily the first time {@link #getObject()} is invoked in
	 * case of a shared singleton; else, on each {@link #getObject()} call.
	 * @return the object returned by this factory
	 * @see #process(java.util.Map, MatchCallback)
	 */
	protected Map<String, Object> createProperties() {
		final Map<String, Object> result = new LinkedHashMap<String, Object>();
		process(new MatchCallback() {
			@Override
			public void process(Properties properties, Map<String, Object> map) {
				merge(result, map);
			}
		});
		return result;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	private void merge(Map<String, Object> output, Map<String, Object> map) {
		for (Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			Object existing = output.get(key);
			if (value instanceof Map && existing instanceof Map) {
				Map<String, Object> result = new LinkedHashMap<String, Object>(
						(Map) existing);
				merge(result, (Map) value);
				output.put(key, result);
			}
			else {
				output.put(key, value);
			}
		}
	}

}