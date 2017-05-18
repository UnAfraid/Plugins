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
package com.github.unafraid.plugins.installers;

import com.github.unafraid.plugins.AbstractPlugin;
import com.github.unafraid.plugins.exceptions.PluginException;

/**
 * A simple interface to provide a possibility to register several types of custom installers.
 * @author UnAfraid
 */
public interface IPluginInstaller
{
	/**
	 * Triggered whenever the plugin is being installed.
	 * @param plugin the plugin
	 * @throws PluginException
	 */
	public void install(AbstractPlugin plugin) throws PluginException;
	
	/**
	 * Triggered whenever the plugin is being uninstalled.
	 * @param plugin the plugin
	 * @throws PluginException
	 */
	public void uninstall(AbstractPlugin plugin) throws PluginException;
}
