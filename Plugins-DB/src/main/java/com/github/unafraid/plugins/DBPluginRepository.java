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
package com.github.unafraid.plugins;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.unafraid.plugins.db.DatabaseProvider;
import com.github.unafraid.plugins.db.dao.PluginsDAO;
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
	 * An event triggered whenever the application is booting.<br>
	 * This is designed explicitly to start previously [before application shutdown] started plugin(s) only.
	 */
	public void onApplicationBoot()
	{
		try (PluginsDAO pluginsDao = DatabaseProvider.DBI.open(PluginsDAO.class))
		{
			final List<Plugin> installedPlugins = pluginsDao.findAll();
			
			//@formatter:off
			getAvailablePlugins().filter(plugin -> installedPlugins.stream().anyMatch(dbPlugin -> 
				dbPlugin.getName().equalsIgnoreCase(plugin.getName()) 
				&& ((dbPlugin.getVersion() == plugin.getVersion()) 
				&& (dbPlugin.isAutoStart()))))
				.forEach(plugin -> {
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
			//@formatter:on
		}
	}
	
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
		try (PluginsDAO pluginsDao = DatabaseProvider.DBI.open(PluginsDAO.class))
		{
			final List<Plugin> installedPlugins = pluginsDao.findAll();
			
			//@formatter:off
			return getAvailablePlugins()
				.filter(plugin -> installedPlugins.stream().anyMatch(dbPlugin -> dbPlugin.getName().equalsIgnoreCase(plugin.getName()) && (dbPlugin.getVersion() == plugin.getVersion())));
			//@formatter:on	
		}
	}
	
	/**
	 * Installs the plugin and stores it into the database.
	 * @param plugin
	 * @throws PluginException
	 */
	public void installPlugin(AbstractDBPlugin plugin) throws PluginException
	{
		Objects.requireNonNull(plugin);
		
		try (PluginsDAO pluginsDao = DatabaseProvider.DBI.open(PluginsDAO.class))
		{
			final Plugin dbPlugin = pluginsDao.findByName(plugin.getName());
			if (dbPlugin != null)
			{
				throw new PluginException("Plugin is already installed!");
			}
			
			plugin.install();
			
			pluginsDao.insert(plugin.getName(), plugin.getVersion(), System.currentTimeMillis(), 0);
		}
	}
	
	/**
	 * Uninstalls the plugin and removes it from the database.
	 * @param plugin
	 * @throws PluginException
	 */
	public void uninstallPlugin(AbstractDBPlugin plugin) throws PluginException
	{
		Objects.requireNonNull(plugin);
		
		try (PluginsDAO pluginsDao = DatabaseProvider.DBI.open(PluginsDAO.class))
		{
			final Plugin dbPlugin = pluginsDao.findByName(plugin.getName());
			if (dbPlugin == null)
			{
				throw new PluginException("Plugin is not installed yet!");
			}
			
			plugin.uninstall();
			
			pluginsDao.delete(dbPlugin.getId());
		}
	}
}