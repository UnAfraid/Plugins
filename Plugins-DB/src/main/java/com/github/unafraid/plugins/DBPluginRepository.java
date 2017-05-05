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
package com.github.unafraid.plugins;

import java.util.List;
import java.util.stream.Stream;

import com.github.unafraid.plugins.data.sql.dao.PluginsDAO;
import com.github.unafraid.plugins.data.sql.dao.dto.Plugin;

/**
 * @author UnAfraid
 * @param <T>
 */
public class DBPluginRepository<T extends AbstractPlugin> extends PluginRepository<T>
{
	private static final PluginsDAO PLUGINS_DAO = DBIProvider.DBI.open(PluginsDAO.class);
	
	public T getInstalledPlugin(String name)
	{
		//@formatter:off
		return getInstalledPlugins()
			.filter(plugin -> name.equalsIgnoreCase(plugin.getName()))
			.findFirst().orElse(null);
		//@formatter:on
	}
	
	public Stream<T> getInstalledPlugins()
	{
		final List<Plugin> installedPlugins = PLUGINS_DAO.findAll();
		
		//@formatter:off
		return getAvailablePlugins()
			.filter(plugin -> installedPlugins.stream().anyMatch(dbPlugin -> dbPlugin.getName().equalsIgnoreCase(plugin.getName())));
		//@formatter:on
	}
}