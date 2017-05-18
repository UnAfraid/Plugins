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

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.unafraid.plugins.exceptions.PluginException;

/**
 * This is the class that scans for available plugins.<br>
 * You can assume that it is the plugin manager. However it is a little but more.
 * @author UnAfraid
 * @param <T> refers to your own {@link AbstractPlugin} implementation abstract class, or you can use the original also
 */
public class PluginRepository<T extends AbstractPlugin>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginRepository.class);
	private final Map<String, Map<Integer, T>> _plugins = new HashMap<>();
	private final Map<T, ClassLoader> _classLoaders = new HashMap<>();
	
	/**
	 * This method scans your classpath for the available plugins that can be initialized.<br>
	 * If you aren't using IDE, you may drop your plugin JARs into "plugins" directory.
	 * @param pluginClass here you shall provide the very same class you provided above as {@code T}
	 */
	public final void scan(Class<T> pluginClass)
	{
		// Scan for plug-ins deployed as 'jar' files
		final int previousSize = _plugins.size();
		try
		{
			final Path plugins = Paths.get("plugins");
			if (Files.isDirectory(plugins))
			{
				//@formatter:off
				Files.list(plugins)
					.filter(path -> path.getFileName().toString().endsWith(".jar"))
					.forEach(path -> 
					{
						try
						{
							final URL url = path.toUri().toURL();
							final URLClassLoader classLoader = new URLClassLoader(new URL[] { url });
							ServiceLoader.load(pluginClass, classLoader)
								.forEach(plugin -> processPlugin(plugin, classLoader));
						}
						catch (Exception e)
						{
							LOGGER.warn("Failed to convert path: {} to URI/URL", path, e);
						}
					}
				);
				//@formatter:on
			}
		}
		catch (Exception e)
		{
			LOGGER.warn("Failed to scan for plugins: ", e);
		}
		
		// Scan general class loader for plug-ins (Debug project include)
		//@formatter:off
		ServiceLoader.load(pluginClass)
			.forEach(plugin -> processPlugin(plugin, Thread.currentThread().getContextClassLoader()));
		//@formatter:on
		
		if (previousSize != _plugins.size())
		{
			LOGGER.info("Discovered {} -> {} plugin(s).", previousSize, _plugins.size());
		}
		else if (_plugins.size() != 0)
		{
			LOGGER.info("Reloaded {} plugin(s).", _plugins.size());
		}
	}
	
	/**
	 * Processes the plugin into the plugin repository.
	 * @param plugin the plugin
	 * @param classLoader the class loader of the plugin
	 */
	private void processPlugin(T plugin, ClassLoader classLoader)
	{
		final Map<Integer, T> plugins = _plugins.computeIfAbsent(plugin.getName(), key -> new HashMap<>());
		if (!plugins.containsKey(plugin.getVersion()))
		{
			final T oldPlugin = plugins.put(plugin.getVersion(), plugin);
			if (oldPlugin != null)
			{
				// After re-scan plugin might be changed, so stop first.
				if (oldPlugin.getState() == PluginState.STARTED)
				{
					try
					{
						oldPlugin.stop();
					}
					catch (PluginException e)
					{
						LOGGER.warn("Failed to stop old plugin {}", plugin.getName(), e);
					}
					
					// start again
					if (oldPlugin.getVersion() == plugin.getVersion())
					{
						try
						{
							plugin.start();
						}
						catch (PluginException e)
						{
							LOGGER.warn("Failed to start new plugin {}", plugin.getName(), e);
						}
					}
				}
			}
			_classLoaders.put(plugin, classLoader);
		}
	}
	
	/**
	 * Gets a {@link Map} view of all plugins.
	 * @return all plugins
	 */
	public final Map<String, Map<Integer, T>> getAllPlugins()
	{
		return _plugins;
	}
	
	/**
	 * Starts all initialized plugins and setting them to installed.
	 */
	public void startAll()
	{
		getAvailablePlugins().forEach(plugin ->
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
	 * Stops all plugins.
	 */
	public void stopAll()
	{
		getAvailablePlugins().forEach(plugin ->
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
	 * Gets an available plugin by its name.
	 * @param name the plugin's name
	 * @return available plugin
	 */
	public T getAvailablePlugin(String name)
	{
		//@formatter:off
		return getAvailablePlugins()
			.filter(plugin -> name.equalsIgnoreCase(plugin.getName()))
			.findFirst().orElse(null);
		//@formatter:on
	}
	
	/**
	 * Gets a {@link Stream} view of available plugins.
	 * @return available plugins
	 */
	public final Stream<T> getAvailablePlugins()
	{
		//@formatter:off
		return _plugins.values().stream()
			.flatMap(map -> map.values().stream())
			.sorted(Comparator.comparingInt(T::getVersion).reversed());
		//@formatter:on
	}
	
	/**
	 * Gets a class loader by the plugin.
	 * @param plugin the plugin
	 * @return class loader
	 */
	public final ClassLoader getClassLoader(T plugin)
	{
		return _classLoaders.get(plugin);
	}
}
