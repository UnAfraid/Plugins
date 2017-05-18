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

import com.github.unafraid.plugins.conditions.PluginConditions;
import com.github.unafraid.plugins.installers.db.DatabaseInstaller;
import com.github.unafraid.plugins.installers.file.FileInstaller;
import com.github.unafraid.plugins.migrations.PluginMigrations;

/**
 * Database supporting version of {@link AbstractPlugin}.
 * @see AbstractPlugin
 * @author UnAfraid
 */
public abstract class AbstractDBPlugin extends AbstractPlugin
{
	private final DatabaseInstaller _databaseInstaller = new DatabaseInstaller();
	{
		getInstallers().add(_databaseInstaller);
	}
	
	@Override
	protected final void setup(FileInstaller fileInstaller, PluginMigrations migrations, PluginConditions pluginConditions)
	{
		setup(fileInstaller, _databaseInstaller, migrations, pluginConditions);
	}
	
	/**
	 * Triggered whenever the plugin is being initialized.
	 * @param fileInstaller the file installer
	 * @param dbInstaller the database installer
	 * @param migrations the relevant plugin migrations
	 * @param pluginConditions plugin conditions
	 */
	protected abstract void setup(FileInstaller fileInstaller, DatabaseInstaller dbInstaller, PluginMigrations migrations, PluginConditions pluginConditions);
	
	/**
	 * Gets the database installer
	 * @return DB Installer
	 */
	public final DatabaseInstaller getDatabaseInstaller()
	{
		return _databaseInstaller;
	}
}
