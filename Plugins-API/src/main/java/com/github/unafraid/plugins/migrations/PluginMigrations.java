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
import com.github.unafraid.plugins.PluginException;

/**
 * @author UnAfraid
 */
public class PluginMigrations
{
	private final Set<IPluginMigration> _migrations = new HashSet<>();
	
	public void addMigration(IPluginMigration migration)
	{
		_migrations.add(migration);
	}
	
	public Set<IPluginMigration> getMigrations()
	{
		return _migrations;
	}
	
	public void migrate(int from, int to, AbstractPlugin plugin) throws PluginException
	{
		if (from >= to)
		{
			throw new PluginException("Canno migrate from >= to");
		}
		
		//@formatter:off
		_migrations.stream()
			.sorted(Comparator.comparingInt(IPluginMigration::getTargetVersion))
			.filter(migration -> migration.getTargetVersion() >= from)
			.forEach(migration -> migration.migrate(plugin));
		//@formatter:off
	}
}
