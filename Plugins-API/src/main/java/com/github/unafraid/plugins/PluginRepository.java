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
 * @author UnAfraid
 * @param <T>
 */
public class PluginRepository<T extends AbstractPlugin>
{
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginRepository.class);
	private final Map<String, Map<Integer, T>> _plugins = new HashMap<>();
	private final Map<T, ClassLoader> _classLoaders = new HashMap<>();
	
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
			LOGGER.info("Discovered {} -> {} plugins", previousSize, _plugins.size());
		}
	}
	
	private void processPlugin(T plugin, ClassLoader classLoader)
	{
		final Map<Integer, T> plugins = _plugins.computeIfAbsent(plugin.getName(), key -> new HashMap<>());
		if (!plugins.containsKey(plugin.getVersion()))
		{
			plugins.put(plugin.getVersion(), plugin);
			_classLoaders.put(plugin, classLoader);
		}
	}
	
	public final Map<String, Map<Integer, T>> getAllPlugins()
	{
		return _plugins;
	}
	
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
	
	public T getAvailablePlugin(String name)
	{
		//@formatter:off
		return getAvailablePlugins()
			.filter(plugin -> name.equalsIgnoreCase(plugin.getName()))
			.findFirst().orElse(null);
		//@formatter:on
	}
	
	public final Stream<T> getAvailablePlugins()
	{
		//@formatter:off
		return _plugins.values().stream()
			.flatMap(map -> map.values().stream())
			.sorted(Comparator.comparingInt(T::getVersion).reversed());
		//@formatter:on
	}
	
	public final ClassLoader getClassLoader(T plugin)
	{
		return _classLoaders.get(plugin);
	}
}
