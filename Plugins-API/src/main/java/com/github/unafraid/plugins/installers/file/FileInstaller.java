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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.unafraid.plugins.AbstractPlugin;
import com.github.unafraid.plugins.exceptions.PluginException;
import com.github.unafraid.plugins.installers.IPluginInstaller;

/**
 * A simple file installer that handles types of {@link PluginFile} holder class.
 * @author UnAfraid
 */
public class FileInstaller implements IPluginInstaller
{
	protected static final Logger LOGGER = LoggerFactory.getLogger(FileInstaller.class);
	
	private final Set<PluginFile> _files = new HashSet<>();
	private final Set<PluginFile> _directories = new HashSet<>();
	
	/**
	 * Registers a file into this installer.
	 * @param source where installer should look for the original file
	 * @param destination where installer shall put the file
	 */
	public void addFile(String source, String destination)
	{
		_files.add(new PluginFile(source, destination));
	}
	
	/**
	 * Registers a folder into this installer.
	 * @param source where installer should look for the original folder
	 * @param destination where installer shall put the folder
	 */
	public void addFolder(String source, String destination)
	{
		_directories.add(new PluginFile(source, destination));
	}
	
	/**
	 * Gets the registered files inside this installer.
	 * @return files
	 */
	public Set<PluginFile> getFiles()
	{
		return _files;
	}
	
	/**
	 * Gets the registered directories inside this installer.
	 * @return directories
	 */
	public Set<PluginFile> getDirectories()
	{
		return _directories;
	}
	
	@Override
	public void install(AbstractPlugin plugin) throws PluginException
	{
		final URL location = plugin.getClass().getProtectionDomain().getCodeSource().getLocation();
		LOGGER.debug("Location: {} files {}, directories: {}", location, _files, _directories);
		if (location.getProtocol().equals("file"))
		{
			try
			{
				final Path path = Paths.get(location.toURI());
				LOGGER.debug("Path: {}", location);
				if (location.getPath().endsWith(".jar"))
				{
					try (FileSystem fs = FileSystems.newFileSystem(path, Thread.currentThread().getContextClassLoader()))
					{
						for (PluginFile file : _directories)
						{
							installDirectory(fs.getPath("/" + file.getSource()), plugin.getRelativePath(file.getDestination()));
						}
						
						for (PluginFile file : _files)
						{
							installResources(fs.getPath("/" + file.getSource()), plugin.getRelativePath(file.getDestination()));
						}
					}
					catch (Exception e)
					{
						throw new PluginException(e);
					}
				}
				else
				{
					for (PluginFile file : _directories)
					{
						installDirectory(path.resolve(file.getSource().startsWith("/") ? file.getSource().substring(1) : file.getSource()), plugin.getRelativePath(file.getDestination()));
					}
					
					for (PluginFile file : _files)
					{
						installResources(path.resolve(file.getSource().startsWith("/") ? file.getSource().substring(1) : file.getSource()), plugin.getRelativePath(file.getDestination()));
					}
				}
			}
			catch (Exception e)
			{
				throw new PluginException(e);
			}
		}
		else
		{
			throw new PluginException("Source of class " + getClass() + " is not of a file protocol. Source URL: " + location);
		}
	}
	
	/**
	 * Installs the content of source directory into the destination.
	 * @param source where installer should look for the original folder
	 * @param destination where installer shall put the folder
	 * @throws IOException
	 */
	private static void installDirectory(Path source, Path destination) throws IOException
	{
		LOGGER.debug("installResources: {} -> {}", source, destination.toAbsolutePath());
		
		Files.walkFileTree(source, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>()
		{
			@Override
			public FileVisitResult preVisitDirectory(Path sourceDirectory, BasicFileAttributes attrs) throws IOException
			{
				final Path destinationDirectory = destination.resolve(source.relativize(sourceDirectory));
				Files.createDirectories(destinationDirectory);
				LOGGER.debug("Created directories: {}", destinationDirectory);
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult visitFile(Path sourceFile, BasicFileAttributes attrs) throws IOException
			{
				final Path destinationFile = destination.resolve(source.relativize(sourceFile));
				LOGGER.debug("Copying file: {} -> {}", sourceFile, destinationFile);
				Files.copy(sourceFile, destinationFile, StandardCopyOption.REPLACE_EXISTING);
				LOGGER.debug("Copied: {}", destinationFile);
				return FileVisitResult.CONTINUE;
			}
		});
	}
	
	/**
	 * Installs the source file into the destination.
	 * @param source where installer should look for the original file
	 * @param destination where installer shall put the file
	 */
	private static void installResources(final Path source, final Path destination)
	{
		LOGGER.debug("installResources: {} -> {}", source, destination.toAbsolutePath());
		
		try
		{
			if (!Files.exists(destination))
			{
				Files.createDirectories(destination);
			}
			
			LOGGER.debug("Copying file: {} -> {}", source, destination);
			Files.copy(Files.newInputStream(source), destination, StandardCopyOption.REPLACE_EXISTING);
			LOGGER.debug("Copied: {}", destination);
		}
		catch (Exception e)
		{
			LOGGER.warn("", e);
		}
	}
	
	@Override
	public void uninstall(AbstractPlugin plugin) throws PluginException
	{
		try
		{
			for (PluginFile file : _files)
			{
				final Path destFile = plugin.getRelativePath(file.getDestination());
				Files.deleteIfExists(destFile);
				LOGGER.debug("Deleted file: {}", destFile);
			}
			
			for (PluginFile dir : _directories)
			{
				final Path destFile = plugin.getRelativePath(dir.getDestination());
				Files.walkFileTree(destFile, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>()
				{
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
					{
						Files.deleteIfExists(file);
						LOGGER.debug("Deleted file: {}", file);
						return FileVisitResult.CONTINUE;
					}
					
					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
					{
						Files.deleteIfExists(dir);
						LOGGER.debug("Deleted directory: {}", dir);
						return FileVisitResult.CONTINUE;
					}
				});
			}
			
			final Path pluginRoot = plugin.getRelativePath(".");
			if (Files.exists(pluginRoot))
			{
				Files.walkFileTree(pluginRoot, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>()
				{
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
					{
						// Files might be there either as config properties, or left by the user.
						Files.deleteIfExists(file);
						LOGGER.debug("Deleted remaining file: {}", file);
						return FileVisitResult.CONTINUE;
					}
					
					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
					{
						// Cleanup unnecessary parent directories created by single-file installations.
						Files.deleteIfExists(dir);
						LOGGER.debug("Deleted directory: {}", dir);
						return FileVisitResult.CONTINUE;
					}
				});
				
				// Finally delete the empty plugin's root directory also.
				Files.deleteIfExists(pluginRoot);
			}
		}
		catch (Exception e)
		{
			throw new PluginException(e);
		}
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((_directories == null) ? 0 : _directories.hashCode());
		result = (prime * result) + ((_files == null) ? 0 : _files.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		final FileInstaller other = (FileInstaller) obj;
		if (_directories == null)
		{
			if (other._directories != null)
			{
				return false;
			}
		}
		else if (!_directories.equals(other._directories))
		{
			return false;
		}
		if (_files == null)
		{
			if (other._files != null)
			{
				return false;
			}
		}
		else if (!_files.equals(other._files))
		{
			return false;
		}
		return true;
	}
}