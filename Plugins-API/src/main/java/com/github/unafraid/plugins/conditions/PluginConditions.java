/*
 * Copyright (C) 2004-2017 L2J Unity
 * 
 * This file is part of L2J Unity.
 * 
 * L2J Unity is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Unity is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.unafraid.plugins.conditions;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.unafraid.plugins.AbstractPlugin;
import com.github.unafraid.plugins.PluginException;

/**
 * @author UnAfraid
 */
public class PluginConditions
{
	private final Map<ConditionType, Set<IPluginCondition>> _conditions = new EnumMap<>(ConditionType.class);
	
	public void addCondition(ConditionType type, IPluginCondition condition)
	{
		_conditions.computeIfAbsent(type, key -> new HashSet<>()).add(condition);
	}
	
	public Set<IPluginCondition> getConditions(ConditionType type)
	{
		return _conditions.getOrDefault(type, Collections.emptySet());
	}
	
	public void testConditions(ConditionType type, AbstractPlugin plugin) throws PluginException
	{
		for (IPluginCondition condition : getConditions(type))
		{
			final ConditionResult result = condition.test(plugin);
			if ((result == null) || !result.isSuccess())
			{
				throw new PluginException(result == null ? "ConditionResult is null!" : result.describe());
			}
		}
	}
}
