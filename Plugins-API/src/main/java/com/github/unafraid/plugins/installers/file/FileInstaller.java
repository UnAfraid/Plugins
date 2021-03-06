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
package com.github.unafraid.plugins.installers.file;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import com.github.unafraid.plugins.AbstractPlugin;
import com.github.unafraid.plugins.exceptions.PluginException;
import com.github.unafraid.plugins.installers.IPluginInstaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple file installer that handles types of {@link PluginFile} holder class.
 * @author UnAfraid
 */
public class FileInstaller implements IPluginInstaller {
	protected static final Logger LOGGER = LoggerFactory.getLogger(FileInstaller.class);
	
	private final Set<PluginFile> files = new HashSet<>();
	private final Set<PluginFile> directories = new HashSet<>();
	
	/**
	 * Registers a file into this installer.
	 * @param source where installer should look for the original file
	 * @param destination where installer shall put the file
	 */
	public void addFile(String source, String destination) {
		files.add(new PluginFile(source, destination));
	}
	
	/**
	 * Registers a folder into this installer.
	 * @param source where installer should look for the original folder
	 * @param destination where installer shall put the folder
	 */
	public void addFolder(String source, String destination) {
		directories.add(new PluginFile(source, destination));
	}
	
	/**
	 * Gets the registered files inside this installer.
	 * @return files
	 */
	public Set<PluginFile> getFiles() {
		return files;
	}
	
	/**
	 * Gets the registered directories inside this installer.
	 * @return directories
	 */
	public Set<PluginFile> getDirectories() {
		return directories;
	}
	
	@Override
	public void install(AbstractPlugin plugin) throws PluginException {
		processResources(plugin, false);
	}
	
	/**
	 * Processing resource installation or repair based on parameters.
	 * @param plugin the plugin
	 * @param repair set to {@code false}, if install is invoked, set to {@code true} if repair
	 * @throws PluginException
	 */
	private void processResources(AbstractPlugin plugin, boolean repair) throws PluginException {
		final URL location = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
		LOGGER.debug("Location: {} files {}, directories: {}", location, files, directories);
		if (location.getProtocol().equals("file")) {
			try {
				final Path path = Paths.get(location.toURI());
				LOGGER.debug("Path: {}", location);
				if (location.getPath().endsWith(".jar")) {
					LOGGER.debug("Getting resources from JAR.");
					
					try (FileSystem fs = FileSystems.newFileSystem(path, Thread.currentThread().getContextClassLoader())) {
						for (PluginFile file : directories) {
							installDirectory(fs.getPath("/" + file.getSource()), plugin.getRelativePath(file.getDestination()), repair);
						}
						
						for (PluginFile file : files) {
							installResources(fs.getPath("/" + file.getSource()), plugin.getRelativePath(file.getDestination()), repair);
						}
					}
					catch (Exception e) {
						throw new PluginException(e);
					}
				}
				else {
					LOGGER.debug("Getting resources from ClassPath.");
					
					for (PluginFile file : directories) {
						installDirectory(path.resolve(file.getSource().startsWith("/") ? file.getSource().substring(1) : file.getSource()), plugin.getRelativePath(file.getDestination()), repair);
					}
					
					for (PluginFile file : files) {
						installResources(path.resolve(file.getSource().startsWith("/") ? file.getSource().substring(1) : file.getSource()), plugin.getRelativePath(file.getDestination()), repair);
					}
				}
			}
			catch (Exception e) {
				throw new PluginException(e);
			}
		}
		else {
			throw new PluginException("Source of class " + getClass() + " is not of a file protocol. Source URL: " + location);
		}
	}
	
	/**
	 * Installs the content of source directory into the destination.
	 * @param source where installer should look for the original folder
	 * @param destination where installer shall put the folder
	 * @param repair set to {@code false}, if install is invoked, set to {@code true} if repair
	 * @throws IOException
	 */
	private static void installDirectory(Path source, Path destination, boolean repair) throws IOException {
		LOGGER.debug("installDirectory: {} -> {}", source.toAbsolutePath(), destination.toAbsolutePath());
		
		Files.walkFileTree(source, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
			private Path _currentTarget;
			
			@Override
			public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
				_currentTarget = destination.resolve(source.relativize(dir).toString());
				if (Files.notExists(_currentTarget)) {
					Files.createDirectories(_currentTarget);
					LOGGER.debug("Created directories: {}", _currentTarget);
					
					if (repair) {
						LOGGER.warn("Repaired missing plugin directory: {}", _currentTarget);
					}
				}
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attrs) throws IOException {
				final Path destinationFile = destination.resolve(source.relativize(sourceFile).toString());
				if (Files.notExists(destinationFile)) {
					LOGGER.debug("Copying file: {} -> {}", sourceFile, destinationFile);
					Files.copy(Files.newInputStream(sourceFile), destinationFile, StandardCopyOption.REPLACE_EXISTING);
					LOGGER.debug("Copied: {}", destinationFile);
					
					if (repair) {
						LOGGER.warn("Repaired missing plugin file: {}", destinationFile);
					}
				}
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	/**
	 * Installs the source file into the destination.
	 * @param source where installer should look for the original file
	 * @param destination where installer shall put the file
	 * @param repair set to {@code false}, if install is invoked, set to {@code true} if repair
	 */
	private static void installResources(Path source, Path destination, boolean repair) {
		LOGGER.debug("installResources: {} -> {}", source.toAbsolutePath(), destination.toAbsolutePath());
		
		try {
			if (Files.notExists(destination)) {
				Files.createDirectories(destination);
				
				LOGGER.debug("Copying file: {} -> {}", source, destination);
				Files.copy(Files.newInputStream(source), destination, StandardCopyOption.REPLACE_EXISTING);
				LOGGER.debug("Copied: {}", destination);
				
				if (repair) {
					LOGGER.warn("Repaired missing plugin file: {}", destination);
				}
			}
		}
		catch (IOException e) {
			LOGGER.warn("", e);
		}
	}
	
	@Override
	public void repair(AbstractPlugin plugin) throws PluginException {
		processResources(plugin, true);
	}
	
	@Override
	public void uninstall(AbstractPlugin plugin) throws PluginException {
		try {
			for (PluginFile file : files) {
				final Path destFile = plugin.getRelativePath(file.getDestination());
				Files.deleteIfExists(destFile);
				LOGGER.debug("Deleted file: {}", destFile);
			}
			
			for (PluginFile dir : directories) {
				final Path destFile = plugin.getRelativePath(dir.getDestination());
				Files.walkFileTree(destFile, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						Files.deleteIfExists(file);
						LOGGER.debug("Deleted file: {}", file);
						return FileVisitResult.CONTINUE;
					}
					
					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						Files.deleteIfExists(dir);
						LOGGER.debug("Deleted directory: {}", dir);
						return FileVisitResult.CONTINUE;
					}
				});
			}
			
			final Path pluginRoot = plugin.getRelativePath(".");
			if (Files.exists(pluginRoot)) {
				Files.walkFileTree(pluginRoot, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						// Files might be there either as config properties, or left by the user.
						Files.deleteIfExists(file);
						LOGGER.debug("Deleted remaining file: {}", file);
						return FileVisitResult.CONTINUE;
					}
					
					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						// Cleanup unnecessary parent directories created by single-file installations.
						Files.deleteIfExists(dir);
						LOGGER.debug("Deleted remaining directory: {}", dir);
						return FileVisitResult.CONTINUE;
					}
				});
				
				// Finally delete the empty plugin's root directory also.
				Files.deleteIfExists(pluginRoot);
				LOGGER.debug("Deleted plugin root: {}", pluginRoot);
			}
		}
		catch (IOException e) {
			throw new PluginException(e);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((directories == null) ? 0 : directories.hashCode());
		result = (prime * result) + ((files == null) ? 0 : files.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final FileInstaller other = (FileInstaller) obj;
		if (directories == null) {
			if (other.directories != null) {
				return false;
			}
		}
		else if (!directories.equals(other.directories)) {
			return false;
		}
		if (files == null) {
			if (other.files != null) {
				return false;
			}
		}
		else if (!files.equals(other.files)) {
			return false;
		}
		return true;
	}
}