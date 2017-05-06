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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Scanner;
import java.util.Set;

import com.github.unafraid.plugins.AbstractPlugin;
import com.github.unafraid.plugins.DatabaseProvider;
import com.github.unafraid.plugins.PluginException;
import com.github.unafraid.plugins.installers.IPluginInstaller;

/**
 * @author UnAfraid
 */
public class DatabaseInstaller implements IPluginInstaller
{
	private final Set<PluginDatabaseTable> _tables = new HashSet<>();
	
	public void addTable(String source, String name, Optional<String> database)
	{
		_tables.add(new PluginDatabaseTable(source, name, database));
	}
	
	public Set<PluginDatabaseTable> getDatabaseTables()
	{
		return _tables;
	}
	
	@Override
	public void install(AbstractPlugin plugin) throws PluginException
	{
		try (Connection con = DatabaseProvider.DATABASE_FACTORY.getConnection();
			Statement st = con.createStatement())
		{
			// Prevent half-way execution
			con.setAutoCommit(false);
			
			for (PluginDatabaseTable table : _tables)
			{
				String currentDatabase = "";
				try (ResultSet rs = st.executeQuery("SELECT DATABASE()"))
				{
					if (rs.next())
					{
						currentDatabase = rs.getString(1);
					}
				}
				
				if (table.getDatabase().isPresent())
				{
					// Switch database
					st.execute("USE " + table.getDatabase().get());
				}
				
				// Check for table existence
				try (PreparedStatement ps = con.prepareStatement("SHOW TABLES LIKE ?"))
				{
					ps.setString(1, table.getName());
					try (ResultSet rs = ps.executeQuery())
					{
						if (!rs.next())
						{
							try (InputStream inputStream = getClass().getResourceAsStream(table.getSource());
								InputStreamReader reader = new InputStreamReader(inputStream);
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
					}
				}
				
				if (table.getDatabase().isPresent())
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
		throw new PluginException("Not done");
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((_tables == null) ? 0 : _tables.hashCode());
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
		DatabaseInstaller other = (DatabaseInstaller) obj;
		if (_tables == null)
		{
			if (other._tables != null)
			{
				return false;
			}
		}
		else if (!_tables.equals(other._tables))
		{
			return false;
		}
		return true;
	}
}