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

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Stream;

import com.github.unafraid.plugins.exceptions.PluginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the class that scans for available plugins.<br>
 * You can assume that it is the plugin manager. However it is a little but more.
 * @author UnAfraid
 * @param <T> refers to your own {@link AbstractPlugin} implementation abstract class, or you can use the original also
 */
public class PluginRepository<T extends AbstractPlugin> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginRepository.class);
	private final Map<String, Map<Integer, T>> plugins = new HashMap<>();
	private final Map<T, ClassLoader> classLoaders = new HashMap<>();
	
	/**
	 * This method scans your classpath for the available plugins that can be initialized.<br>
	 * If you aren't using IDE, you may drop your plugin JARs into "plugins" directory.
	 * @param pluginClass here you shall provide the very same class you provided above as {@code T}
	 */
	public final void scan(Class<T> pluginClass) {
		Objects.requireNonNull(pluginClass);
		
		// Scan for plug-ins deployed as 'jar' files
		final int previousSize = plugins.size();
		try {
			final Path plugins = Paths.get("plugins");
			if (Files.isDirectory(plugins)) {
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
		catch (Exception e) {
			LOGGER.warn("Failed to scan for plugins: ", e);
		}
		
		// Scan general class loader for plug-ins (Debug project include)
		//@formatter:off
		ServiceLoader.load(pluginClass)
			.forEach(plugin -> processPlugin(plugin, Thread.currentThread().getContextClassLoader()));
		//@formatter:on
		
		if (previousSize != plugins.size()) {
			LOGGER.info("Discovered {} -> {} plugin(s).", previousSize, plugins.size());
		}
		else if (plugins.size() != 0) {
			LOGGER.info("Reloaded {} plugin(s).", plugins.size());
		}
	}
	
	/**
	 * Processes the plugin into the plugin repository.
	 * @param plugin the plugin
	 * @param classLoader the class loader of the plugin
	 */
	private void processPlugin(T plugin, ClassLoader classLoader) {
		Objects.requireNonNull(plugin);
		Objects.requireNonNull(classLoader);
		
		final Map<Integer, T> plugins = this.plugins.computeIfAbsent(plugin.getName(), key -> new HashMap<>());
		if (!plugins.containsKey(plugin.getVersion())) {
			final T oldPlugin = plugins.put(plugin.getVersion(), plugin);
			if (oldPlugin != null) {
				// After re-scan plugin might be changed, so stop first.
				if (oldPlugin.getState() == PluginState.STARTED) {
					try {
						oldPlugin.stop();
					}
					catch (PluginException e) {
						LOGGER.warn("Failed to stop old plugin {}", plugin.getName(), e);
					}
					
					// start again
					if (oldPlugin.getVersion() == plugin.getVersion()) {
						try {
							plugin.start();
						}
						catch (PluginException e) {
							LOGGER.warn("Failed to start new plugin {}", plugin.getName(), e);
						}
					}
				}
			}
			classLoaders.put(plugin, classLoader);
		}
	}
	
	/**
	 * Gets a {@link Map} view of all plugins.
	 * @return all plugins
	 */
	public final Map<String, Map<Integer, T>> getAllPlugins() {
		return plugins;
	}
	
	/**
	 * Starts all initialized plugins and setting them to installed.
	 */
	public void startAll() {
		getAvailablePlugins().forEach(plugin ->
		{
			try {
				if (plugin.setState(PluginState.INITIALIZED, PluginState.INSTALLED)) {
					plugin.start();
				}
			}
			catch (PluginException e) {
				LOGGER.warn("Failed to start plugin {}", plugin.getName(), e);
			}
		});
	}
	
	/**
	 * Stops all plugins.
	 */
	public void stopAll() {
		getAvailablePlugins().forEach(plugin ->
		{
			try {
				plugin.stop();
			}
			catch (PluginException e) {
				LOGGER.warn("Failed to stop plugin {}", plugin.getName(), e);
			}
		});
	}
	
	/**
	 * Gets an available plugin by its name.
	 * @param name the plugin's name
	 * @return available plugin
	 */
	public T getAvailablePlugin(String name) {
		Objects.requireNonNull(name);
		
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
	public final Stream<T> getAvailablePlugins() {
		//@formatter:off
		return plugins.values().stream()
			.flatMap(map -> map.values().stream())
			.sorted(Comparator.comparingInt(T::getVersion).reversed());
		//@formatter:on
	}
	
	/**
	 * Gets a class loader by the plugin.
	 * @param plugin the plugin
	 * @return class loader
	 */
	public final ClassLoader getClassLoader(T plugin) {
		Objects.requireNonNull(plugin);
		return classLoaders.get(plugin);
	}
}
