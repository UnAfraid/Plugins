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
package com.github.unafraid.plugins.migrations;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import com.github.unafraid.plugins.AbstractPlugin;
import com.github.unafraid.plugins.exceptions.PluginException;

/**
 * A class that stores a set of migrations.<br>
 * Migration happens whenever you upgrade your plugin from an older version to a newer.
 * @author UnAfraid
 */
public class PluginMigrations
{
	private final Set<IPluginMigration> _migrations = new HashSet<>();
	
	/**
	 * Registers a migration into this storage class.
	 * @param migration
	 */
	public void addMigration(IPluginMigration migration)
	{
		_migrations.add(migration);
	}
	
	/**
	 * Gets a {@link Set} view of the migrations.
	 * @return migrations
	 */
	public Set<IPluginMigration> getMigrations()
	{
		return _migrations;
	}
	
	/**
	 * A method to start all possible applicable migrations.
	 * @param from the older version
	 * @param to the newer version
	 * @param plugin the plugin
	 * @throws PluginException
	 */
	public void migrate(int from, int to, AbstractPlugin plugin) throws PluginException
	{
		if (from >= to)
		{
			throw new PluginException("Cannot migrate from " + from + " >= to" + to + "!");
		}
		
		//@formatter:off
		_migrations.stream()
			.sorted(Comparator.comparingInt(IPluginMigration::getTargetVersion))
			.filter(migration -> migration.getTargetVersion() >= from)
			.forEach(migration -> migration.migrate(plugin));
		//@formatter:off
	}

	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((_migrations == null) ? 0 : _migrations.hashCode());
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
		final PluginMigrations other = (PluginMigrations) obj;
		if (_migrations == null)
		{
			if (other._migrations != null)
			{
				return false;
			}
		}
		else if (!_migrations.equals(other._migrations))
		{
			return false;
		}
		return true;
	}
}
