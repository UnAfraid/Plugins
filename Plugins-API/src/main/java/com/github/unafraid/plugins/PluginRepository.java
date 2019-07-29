/*
 *  Copyright (c) 2019 Rumen Nikiforov <unafraid89@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */
package com.github.unafraid.plugins;

import java.io.Closeable;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.unafraid.plugins.exceptions.PluginException;
import com.github.unafraid.plugins.util.FileHashUtil;
import com.github.unafraid.plugins.util.JarClassLoader;
import com.github.unafraid.plugins.util.PathUtil;

/**
 * This is the class that scans for available plugins.<br>
 * You can assume that it is the plugin manager. However it is a little but more.
 *
 * @param <T> refers to your own {@link AbstractPlugin} implementation abstract class, or you can use the original also
 * @author UnAfraid
 */
public class PluginRepository<T extends AbstractPlugin> {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginRepository.class);
	
	private static final String IDE_MODE = "IDE Mode.";
	
	private final Map<String, Map<String, T>> plugins = new HashMap<>();
	private final Map<T, ClassLoader> classLoaders = new HashMap<>();
	
	private final Path pluginsPath;
	private final ClassLoader parentClassLoader;
	
	public PluginRepository(Path pluginsPath, ClassLoader parentClassLoader) {
		this.pluginsPath = pluginsPath;
		this.parentClassLoader = parentClassLoader;
	}
	
	public PluginRepository() {
		this(Paths.get("plugins"), null);
	}
	
	/**
	 * This method scans your classpath for the available plugins that can be initialized.<br>
	 * If you aren't using IDE, you may drop your plugin JARs into "plugins" directory.
	 *
	 * @param pluginClass here you shall provide the very same class you provided above as {@code T}
	 */
	public final void scan(Class<T> pluginClass) {
		Objects.requireNonNull(pluginClass);
		
		// Scan for plug-ins deployed as 'jar' files.
		final int previousSize = plugins.size();
		try {
			if (Files.isDirectory(pluginsPath)) {
				Files.list(pluginsPath)
						.filter(path -> path.getFileName().toString().endsWith(".jar"))
						.forEach(path ->
								{
									try {
										final URL url = path.toUri().toURL();
										final JarClassLoader classLoader = parentClassLoader != null ? new JarClassLoader(new URL[]{url}, parentClassLoader) : new JarClassLoader(new URL[]{url});
										for (T plugin : ServiceLoader.load(pluginClass, classLoader)) {
											if (classLoader.hasParentLoadedClass(plugin.getClass().getName())) {
												// Skip plugins that are coming already known to parent ClassLoaders as they will be loaded below
												continue;
											}
											
											plugin.setPluginsPath(pluginsPath);
											plugin.setJarPath(path);
											plugin.setJarHash(FileHashUtil.getFileHash(path).toString());
											
											try {
												processPlugin(plugin, classLoader);
											} catch (Exception e) {
												LOGGER.warn("Failed to process plugin {}.", plugin, e);
											}
										}
									} catch (Exception e) {
										LOGGER.warn("Failed to convert path: {} to URI/URL", path, e);
									}
								}
						);
			}
		} catch (Exception e) {
			LOGGER.warn("Failed to scan for plugins: ", e);
		}
		
		// Scan general class loader for plug-ins (Debug project include)
		for (T plugin : ServiceLoader.load(pluginClass)) {
			plugin.setPluginsPath(pluginsPath);
			plugin.setJarPath(PathUtil.getClassLocation(pluginClass));
			plugin.setJarHash(IDE_MODE);
			
			try {
				final Map<String, T> plugins = this.plugins.get(plugin.getName());
				if (!plugins.isEmpty()) {
					LOGGER.info("Discarding plugins {} in favor of class-path registered ones", plugins);
					plugins.clear();
				}
				processPlugin(plugin, Thread.currentThread().getContextClassLoader());
			} catch (Exception e) {
				LOGGER.warn("Failed to process plugin {}.", plugin, e);
			}
		}
		
		if (previousSize != plugins.size()) {
			LOGGER.info("Discovered {} -> {} plugin(s).", previousSize, plugins.size());
		} else if (plugins.size() != 0) {
			LOGGER.info("Reloaded {} plugin(s).", plugins.size());
		}
	}
	
	/**
	 * Processes the plugin into the plugin repository.
	 *
	 * @param plugin      the plugin
	 * @param classLoader the class loader of the plugin
	 */
	private void processPlugin(T plugin, ClassLoader classLoader) {
		Objects.requireNonNull(plugin);
		Objects.requireNonNull(classLoader);
		Objects.requireNonNull(plugin.getJarPath());
		
		final String jarHash = plugin.getJarHash();
		Objects.requireNonNull(jarHash);
		
		final Map<String, T> plugins = this.plugins.computeIfAbsent(plugin.getName(), k -> new HashMap<>());
		if (plugins.containsKey(jarHash)) {
			// Do not process unchanged plugins.
			return;
		}
		
		plugins.put(jarHash, plugin);
		classLoaders.put(plugin, classLoader);
	}
	
	/**
	 * Unloads the plugin from the repository, so it can be scanned again.
	 *
	 * @param plugin the plugin that is going to be unloaded
	 * @throws PluginException
	 */
	public void unload(T plugin) throws PluginException {
		if (plugin.getState() == PluginState.STARTED) {
			plugin.stop();
		}
		
		plugins.remove(plugin.getName());
		cleanupClassLoader(plugin);
	}
	
	/**
	 * Closes the classloader which isn't needed anymore, and also removes it from the map.
	 *
	 * @param plugin the plugin whose class loader needs to be cleaned
	 * @throws PluginException
	 */
	private void cleanupClassLoader(T plugin) throws PluginException {
		final ClassLoader classLoader = classLoaders.get(plugin);
		if (classLoader == null) {
			return;
		}
		
		if (classLoader instanceof Closeable) {
			try {
				((Closeable) classLoader).close();
			} catch (IOException e) {
				throw new PluginException(e);
			}
		}
		
		classLoaders.remove(plugin);
	}
	
	/**
	 * Gets a {@link Map} view of all plugins.
	 *
	 * @return all plugins
	 */
	public final Map<String, Map<String, T>> getAllPlugins() {
		return plugins;
	}
	
	/**
	 * Starts all initialized plugins and setting them to installed.
	 */
	public void startAll() {
		getAvailablePlugins().forEach(plugin -> {
			try {
				if (plugin.setState(PluginState.INITIALIZED, PluginState.INSTALLED)) {
					plugin.start();
				}
			} catch (PluginException e) {
				LOGGER.warn("Failed to start plugin {}", plugin.getName(), e);
			}
		});
	}
	
	/**
	 * Stops all plugins.
	 */
	public void stopAll() {
		getAvailablePlugins().forEach(plugin -> {
			try {
				plugin.stop();
			} catch (PluginException e) {
				LOGGER.warn("Failed to stop plugin {}", plugin.getName(), e);
			}
		});
	}
	
	/**
	 * Gets an available plugin by its name.
	 *
	 * @param name the plugin's name
	 * @return available plugin
	 */
	public T getAvailablePlugin(String name) {
		Objects.requireNonNull(name);
		
		return getAvailablePlugins()
				.filter(plugin -> name.equalsIgnoreCase(plugin.getName()))
				.findFirst()
				.orElse(null);
	}
	
	/**
	 * Gets a {@link Stream} view of available plugins.
	 *
	 * @return available plugins
	 */
	public final Stream<T> getAvailablePlugins() {
		return plugins.values()
				.stream()
				.flatMap(map -> map.values().stream())
				.distinct()
				.sorted(Comparator.comparing(T::getJarHash).reversed());
	}
	
	/**
	 * Gets a class loader by the plugin.
	 *
	 * @param plugin the plugin
	 * @return class loader
	 */
	public final ClassLoader getClassLoader(T plugin) {
		Objects.requireNonNull(plugin);
		return classLoaders.get(plugin);
	}
}
