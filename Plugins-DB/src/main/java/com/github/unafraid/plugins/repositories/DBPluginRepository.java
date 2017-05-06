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
package com.github.unafraid.plugins.repositories;

import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.unafraid.plugins.AbstractPlugin;
import com.github.unafraid.plugins.PluginState;
import com.github.unafraid.plugins.db.DatabaseProvider;
import com.github.unafraid.plugins.db.dao.PluginsDAO;
import com.github.unafraid.plugins.db.dao.dto.Plugin;
import com.github.unafraid.plugins.exceptions.PluginException;

/**
 * @author UnAfraid
 * @param <T>
 */
public class DBPluginRepository<T extends AbstractPlugin> extends PluginRepository<T>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DBPluginRepository.class);
	
	private static final PluginsDAO PLUGINS_DAO = DatabaseProvider.DBI.open(PluginsDAO.class);
	
	@Override
	public void startAll()
	{
		getInstalledPlugins().forEach(plugin ->
		{
			try
			{
				if (plugin.setState(PluginState.INITIALIZED, PluginState.INSTALLED))
				{
					plugin.start();
				}
			}
			catch (PluginException e)
			{
				LOGGER.warn("Failed to start plugin {}", plugin.getName(), e);
			}
		});
	}
	
	@Override
	public void stopAll()
	{
		getInstalledPlugins().forEach(plugin ->
		{
			try
			{
				plugin.stop();
			}
			catch (PluginException e)
			{
				LOGGER.warn("Failed to stop plugin {}", plugin.getName(), e);
			}
		});
	}
	
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