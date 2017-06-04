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

import java.util.Objects;
import java.util.Optional;

import com.github.unafraid.plugins.conditions.PluginConditions;
import com.github.unafraid.plugins.db.DatabaseProvider;
import com.github.unafraid.plugins.db.dao.PluginsDAO;
import com.github.unafraid.plugins.db.dao.dto.Plugin;
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
		Objects.requireNonNull(fileInstaller);
		Objects.requireNonNull(migrations);
		Objects.requireNonNull(pluginConditions);
		
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
	
	/**
	 * Gets the plugin database entry if exists
	 * @return The plugin database entry
	 */
	public Optional<Plugin> getDatabaseEntry()
	{
		try (PluginsDAO pluginsDao = DatabaseProvider.DBI.open(PluginsDAO.class))
		{
			return Optional.ofNullable(pluginsDao.findByName(getName()));
		}
	}
}
