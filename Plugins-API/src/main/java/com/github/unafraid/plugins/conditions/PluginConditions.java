/*
 * Copyright (c) 2017 Rumen Nikiforov <unafraid89@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.unafraid.plugins.conditions;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.unafraid.plugins.AbstractPlugin;
import com.github.unafraid.plugins.exceptions.PluginException;

/**
 * Stores the conditions.
 * @author UnAfraid
 */
public class PluginConditions {
	private final Map<ConditionType, Set<IPluginCondition>> conditions = new EnumMap<>(ConditionType.class);
	
	/**
	 * Registers a condition into this storage class if it isn't there already.
	 * @param type type of the condition
	 * @param condition the condition itself
	 */
	public void addCondition(ConditionType type, IPluginCondition condition) {
		conditions.computeIfAbsent(type, key -> new HashSet<>()).add(condition);
	}
	
	/**
	 * Gets condition by the type it is registered.
	 * @param type the type given by the user
	 * @return conditions
	 */
	public Set<IPluginCondition> getConditions(ConditionType type) {
		return conditions.getOrDefault(type, Collections.emptySet());
	}
	
	/**
	 * Fires conditions by type on the plugin.
	 * @param type the given type
	 * @param plugin the plugin
	 * @throws PluginException
	 */
	public void testConditions(ConditionType type, AbstractPlugin plugin) throws PluginException {
		for (IPluginCondition condition : getConditions(type)) {
			final ConditionResult result = condition.test(plugin);
			if ((result == null) || !result.isSuccess()) {
				throw new PluginException(result == null ? "ConditionResult is null!" : result.describe());
			}
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((conditions == null) ? 0 : conditions.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PluginConditions other = (PluginConditions) obj;
		if (conditions == null) {
			if (other.conditions != null) {
				return false;
			}
		}
		else if (!conditions.equals(other.conditions)) {
			return false;
		}
		return true;
	}
}
