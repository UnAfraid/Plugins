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
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.unafraid.plugins.db.DatabaseProvider;
import com.github.unafraid.plugins.db.dao.dto.Plugin;
import com.github.unafraid.plugins.exceptions.PluginException;

/**
 * The database supporting version of {@link PluginRepository}.<br>
 * This way you will be able to store the installed plugins in the database, and also load them from it.
 * @author UnAfraid
 * @param <T> refers to your own {@link AbstractDBPlugin} implementation abstract class, or you can use the original also
 */
public class DBPluginRepository<T extends AbstractPlugin> extends PluginRepository<T>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DBPluginRepository.class);
	
	/**
	 * Starts all installed plugins.
	 */
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
	
	/**
	 * Stops all running plugins.
	 */
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
	
	/**
	 * Gets an installed plugin by its name.
	 * @param name the plugin's name
	 * @return the installed plugin
	 */
	public T getInstalledPlugin(String name)
	{
		Objects.requireNonNull(name);
		
		//@formatter:off
		return getInstalledPlugins()
			.filter(plugin -> name.equalsIgnoreCase(plugin.getName()))
			.findFirst().orElse(null);
		//@formatter:on
	}
	
	/**
	 * Gets a {@link Stream} view of the installed plugins.
	 * @return installed plugins
	 */
	public Stream<T> getInstalledPlugins()
	{
		final List<Plugin> installedPlugins = DatabaseProvider.PLUGINS_DAO.findAll();
		
		//@formatter:off
		return getAvailablePlugins()
			.filter(plugin -> installedPlugins.stream().anyMatch(dbPlugin -> dbPlugin.getName().equalsIgnoreCase(plugin.getName()) && (dbPlugin.getVersion() == plugin.getVersion())));
		//@formatter:on
	}
	
	/**
	 * Installs the plugin and stores it into the database.
	 * @param plugin
	 * @throws PluginException
	 */
	public void installPlugin(AbstractDBPlugin plugin) throws PluginException
	{
		Objects.requireNonNull(plugin);
		
		final Plugin dbPlugin = DatabaseProvider.PLUGINS_DAO.findByName(plugin.getName());
		if (dbPlugin != null)
		{
			throw new PluginException("Plugin is already installed!");
		}
		
		plugin.install();
		
		DatabaseProvider.PLUGINS_DAO.insert(plugin.getName(), plugin.getVersion());
	}
	
	/**
	 * Uninstalls the plugin and removes it from the database.
	 * @param plugin
	 * @throws PluginException
	 */
	public void uninstallPlugin(AbstractDBPlugin plugin) throws PluginException
	{
		Objects.requireNonNull(plugin);
		
		final Plugin dbPlugin = DatabaseProvider.PLUGINS_DAO.findByName(plugin.getName());
		if (dbPlugin == null)
		{
			throw new PluginException("Plugin is not installed yet!");
		}
		
		plugin.uninstall();
		
		DatabaseProvider.PLUGINS_DAO.delete(dbPlugin.getId());
	}
}