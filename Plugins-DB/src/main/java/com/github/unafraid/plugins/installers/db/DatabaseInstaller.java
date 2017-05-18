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
package com.github.unafraid.plugins.installers.db;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import com.github.unafraid.plugins.AbstractPlugin;
import com.github.unafraid.plugins.db.DatabaseProvider;
import com.github.unafraid.plugins.exceptions.PluginException;
import com.github.unafraid.plugins.installers.IPluginInstaller;

/**
 * Database installer that allows database queries upon install/uninstall, executed automatically as plugin is installed or uninstalled
 * @author UnAfraid
 */
public class DatabaseInstaller implements IPluginInstaller
{
	private final Set<PluginDatabaseFile> _installFiles = new HashSet<>();
	private final Set<PluginDatabaseFile> _uninstallFiles = new HashSet<>();
	
	/**
	 * Registers install file scripts, executed when install of plugin is requested
	 * @param source the source of the resource file to execute
	 * @param name if present makes sure table doesn't exists before executing resource file
	 * @param database if present switches the database over to specified one before executing resource file
	 */
	public void addTable(String source, Optional<String> name, Optional<String> database)
	{
		_installFiles.add(new PluginDatabaseFile(source, name, database));
	}
	
	/**
	 * Registers uninstall file script, executed when uninstall of plugin is requested
	 * @param source the source of the resource file to execute
	 * @param name if present makes sure table exists before executing resource file
	 * @param database if present switches the database over to specified one before executing resource file
	 */
	public void addUninstallFile(String source, Optional<String> name, Optional<String> database)
	{
		_uninstallFiles.add(new PluginDatabaseFile(source, name, database));
	}
	
	/**
	 * @return Set of database plugin files that are going to be executed when plugin install is requested
	 */
	public Set<PluginDatabaseFile> getInstallFiles()
	{
		return _installFiles;
	}
	
	/**
	 * @return Set of database plugin files that are going to be executed when plugin uninstall is requested
	 */
	public Set<PluginDatabaseFile> getUninstallFiles()
	{
		return _uninstallFiles;
	}
	
	@Override
	public void install(AbstractPlugin plugin) throws PluginException
	{
		Objects.requireNonNull(plugin);
		
		try (Connection con = DatabaseProvider.DATABASE_FACTORY.getConnection();
			Statement st = con.createStatement())
		{
			// Prevent half-way execution
			con.setAutoCommit(false);
			
			for (PluginDatabaseFile file : _installFiles)
			{
				String currentDatabase = "";
				try (ResultSet rs = st.executeQuery("SELECT DATABASE()"))
				{
					if (rs.next())
					{
						currentDatabase = rs.getString(1);
					}
				}
				
				if (file.getDatabase().isPresent())
				{
					// Switch database
					st.execute("USE " + file.getDatabase().get());
				}
				
				// Check for table existence
				if (file.getName().isPresent())
				{
					try (PreparedStatement ps = con.prepareStatement("SHOW TABLES LIKE ?"))
					{
						ps.setString(1, file.getName().get());
						try (ResultSet rs = ps.executeQuery())
						{
							if (!rs.next())
							{
								// Execute the resource
								executeResource(file.getSource(), st);
							}
						}
					}
				}
				else
				{
					// Execute the resource
					executeResource(file.getSource(), st);
				}
				
				if (file.getDatabase().isPresent())
				{
					// Switch database back to its original state
					st.execute("USE " + currentDatabase);
				}
			}
			
			if (!con.getAutoCommit())
			{
				con.commit();
				con.setAutoCommit(true);
			}
		}
		catch (Exception e)
		{
			throw new PluginException(e);
		}
	}
	
	@Override
	public void uninstall(AbstractPlugin plugin) throws PluginException
	{
		Objects.requireNonNull(plugin);
		
		try (Connection con = DatabaseProvider.DATABASE_FACTORY.getConnection();
			Statement st = con.createStatement())
		{
			// Prevent half-way execution
			con.setAutoCommit(false);
			
			for (PluginDatabaseFile file : _uninstallFiles)
			{
				String currentDatabase = "";
				try (ResultSet rs = st.executeQuery("SELECT DATABASE()"))
				{
					if (rs.next())
					{
						currentDatabase = rs.getString(1);
					}
				}
				
				if (file.getDatabase().isPresent())
				{
					// Switch database
					st.execute("USE " + file.getDatabase().get());
				}
				
				// Check for table existence
				if (file.getName().isPresent())
				{
					try (PreparedStatement ps = con.prepareStatement("SHOW TABLES LIKE ?"))
					{
						ps.setString(1, file.getName().get());
						try (ResultSet rs = ps.executeQuery())
						{
							if (rs.next())
							{
								// Execute the resource
								executeResource(file.getSource(), st);
							}
						}
					}
				}
				else
				{
					// Execute the resource
					executeResource(file.getSource(), st);
				}
				if (file.getDatabase().isPresent())
				{
					// Switch database back to its original state
					st.execute("USE " + currentDatabase);
				}
			}
			
			if (!con.getAutoCommit())
			{
				con.commit();
				con.setAutoCommit(true);
			}
		}
		catch (Exception e)
		{
			throw new PluginException(e);
		}
	}
	
	/**
	 * Executes the source resource file into the statement provided
	 * @param source
	 * @param st
	 * @throws Exception
	 */
	private void executeResource(String source, Statement st) throws Exception
	{
		Objects.requireNonNull(source);
		Objects.requireNonNull(st);
		
		try (InputStream inputStream = getClass().getResourceAsStream(source);
			InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
			Scanner scn = new Scanner(reader))
		{
			StringBuilder sb = new StringBuilder();
			while (scn.hasNextLine())
			{
				String line = scn.nextLine();
				if (line.startsWith("--"))
				{
					continue;
				}
				else if (line.contains("--"))
				{
					line = line.split("--")[0];
				}
				
				line = line.trim();
				if (!line.isEmpty())
				{
					sb.append(line + System.lineSeparator());
				}
				
				if (line.endsWith(";"))
				{
					st.execute(sb.toString());
					sb = new StringBuilder();
				}
			}
		}
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((_installFiles == null) ? 0 : _installFiles.hashCode());
		result = (prime * result) + ((_uninstallFiles == null) ? 0 : _uninstallFiles.hashCode());
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
		final DatabaseInstaller other = (DatabaseInstaller) obj;
		if (_installFiles == null)
		{
			if (other._installFiles != null)
			{
				return false;
			}
		}
		else if (!_installFiles.equals(other._installFiles))
		{
			return false;
		}
		if (_uninstallFiles == null)
		{
			if (other._uninstallFiles != null)
			{
				return false;
			}
		}
		else if (!_uninstallFiles.equals(other._uninstallFiles))
		{
			return false;
		}
		return true;
	}
}