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
package com.github.unafraid.plugins.installers.file;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.unafraid.plugins.AbstractPlugin;
import com.github.unafraid.plugins.exceptions.PluginException;
import com.github.unafraid.plugins.installers.IPluginInstaller;

/**
 * @author UnAfraid
 */
public class FileInstaller implements IPluginInstaller
{
	private static final Logger LOGGER = LoggerFactory.getLogger(FileInstaller.class);
	
	private final Set<PluginFile> _files = new HashSet<>();
	private final Set<PluginFile> _directories = new HashSet<>();
	
	public void addFile(String source, String destination)
	{
		_files.add(new PluginFile(source, destination));
	}
	
	public void addFolder(String source, String destination)
	{
		_directories.add(new PluginFile(source, destination));
	}
	
	public Set<PluginFile> getFiles()
	{
		return _files;
	}
	
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
	
	private void installDirectory(final Path source, final Path destination) throws IOException
	{
		LOGGER.debug("installResources: {} -> {}", source, destination.toAbsolutePath());
		
		Files.walk(source).forEach(path ->
		{
			try
			{
				if (!Files.exists(destination))
				{
					Files.createDirectories(destination);
				}
				
				final Path dst = destination.resolve(path.normalize());
				if (!Files.exists(dst.getParent()))
				{
					Files.createDirectories(dst.getParent());
					LOGGER.debug("Created parent: {}", dst.getParent());
				}
				else
				{
					LOGGER.debug("Directory already exists: {}", dst.getParent());
				}
				LOGGER.debug("Copying file: {} -> {}", source, dst);
				Files.copy(Files.newInputStream(source), dst, StandardCopyOption.REPLACE_EXISTING);
				LOGGER.debug("Copied: {}", dst);
			}
			catch (Exception e)
			{
				LOGGER.warn("", e);
			}
		});
	}
	
	private void installResources(final Path source, final Path destination)
	{
		LOGGER.debug("installResources: {} -> {}", source, destination.toAbsolutePath());
		
		try
		{
			if (!Files.exists(destination))
			{
				Files.createDirectories(destination);
			}
			
			final Path dst = destination;
			if (!Files.exists(dst.getParent()))
			{
				Files.createDirectories(dst.getParent());
				LOGGER.debug("Created parent: {}", dst.getParent());
			}
			else
			{
				LOGGER.debug("Directory already exists: {}", dst.getParent());
			}
			LOGGER.debug("Copying file: {} -> {}", source, dst);
			Files.copy(Files.newInputStream(source), dst, StandardCopyOption.REPLACE_EXISTING);
			LOGGER.debug("Copied: {}", dst);
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
				final Path destFile = Paths.get(file.getDestination());
				Files.deleteIfExists(destFile);
			}
			
			for (PluginFile dir : _directories)
			{
				final Path destFile = Paths.get(dir.getDestination());
				Files.walkFileTree(destFile, new SimpleFileVisitor<Path>()
				{
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
					{
						Files.deleteIfExists(file);
						return FileVisitResult.CONTINUE;
					}
					
					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException
					{
						if (exc != null)
						{
							throw exc;
						}
						Files.deleteIfExists(dir);
						return FileVisitResult.CONTINUE;
					}
				});
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