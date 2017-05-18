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
package com.github.unafraid.plugins.migrations;

import com.github.unafraid.plugins.AbstractPlugin;

/**
 * An interface to be used to create plugin migrations.
 * @author UnAfraid
 */
public interface IPluginMigration
{
	/**
	 * Gets the description of this migration.<br>
	 * In short: the reason of change, what happened, etc.
	 * @return description
	 */
	public String getDescription();
	
	/**
	 * The target version where migrations is applicable.
	 * @return target version
	 */
	public int getTargetVersion();
	
	/**
	 * Triggered whenever the plugin is being migrated.
	 * @param plugin the plugin
	 */
	public void migrate(AbstractPlugin plugin);
}
