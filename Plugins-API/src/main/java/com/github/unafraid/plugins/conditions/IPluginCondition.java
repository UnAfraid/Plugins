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

import com.github.unafraid.plugins.AbstractPlugin;

/**
 * The interface that is used to create a condition.
 * @author UnAfraid
 */
public interface IPluginCondition
{
	/**
	 * Gets the information related to this condition.
	 * @return information
	 */
	public String getInformation();
	
	/**
	 * Fires the condition.
	 * @param plugin the plugin
	 * @return condition result
	 */
	public ConditionResult test(AbstractPlugin plugin);
}