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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

import com.github.unafraid.plugins.conditions.ConditionType;
import com.github.unafraid.plugins.conditions.PluginConditions;
import com.github.unafraid.plugins.installers.db.DatabaseInstaller;
import com.github.unafraid.plugins.installers.file.FileInstaller;
import com.github.unafraid.plugins.migrations.PluginMigrations;

/**
 * @author UnAfraid
 */
public abstract class AbstractPlugin
{
	private final PluginConditions _conditions = new PluginConditions();
	private final DatabaseInstaller _databaseInstaller = new DatabaseInstaller();
	private final FileInstaller _fileInstaller = new FileInstaller();
	private final PluginMigrations _migrations = new PluginMigrations();
	private final AtomicReference<PluginState> _state = new AtomicReference<>(PluginState.AVAILABLE);
	
	public abstract String getName();
	
	public abstract String getAuthor();
	
	public abstract String getCreatedAt();
	
	public abstract String getDescription();
	
	public abstract int getVersion();
	
	protected abstract void setup(FileInstaller fileInstaller, DatabaseInstaller dbInstaller, PluginMigrations migrations, PluginConditions pluginConditions);
	
	protected abstract void initialize();
	
	public abstract void shutdown();
	
	public final boolean setState(PluginState currentState, PluginState newState)
	{
		return _state.compareAndSet(currentState, newState);
	}
	
	public final PluginState getState()
	{
		return _state.get();
	}
	
	protected final void init()
	{
		setup(_fileInstaller, _databaseInstaller, _migrations, _conditions);
	}
	
	public final void start() throws PluginException
	{
		_conditions.testConditions(ConditionType.RUNTIME, this);
		initialize();
	}
	
	public final void install() throws PluginException
	{
		_conditions.testConditions(ConditionType.INSTALL, this);
		_databaseInstaller.install(this);
		_fileInstaller.install(this);
	}
	
	public final void uninstall() throws PluginException
	{
		_databaseInstaller.uninstall(this);
		_fileInstaller.uninstall(this);
	}
	
	public final void migrate(int from, int to) throws PluginException
	{
		_migrations.migrate(from, to, this);
	}
	
	public final PluginConditions getConditions()
	{
		return _conditions;
	}
	
	public final DatabaseInstaller getDatabaseInstaller()
	{
		return _databaseInstaller;
	}
	
	public final FileInstaller getFileInstaller()
	{
		return _fileInstaller;
	}
	
	public final PluginMigrations getMigrations()
	{
		return _migrations;
	}
	
	public final Path getAbsolutePath(String... paths)
	{
		final String[] totalPaths = new String[paths.length + 2];
		totalPaths[0] = "plugins";
		totalPaths[1] = getName();
		System.arraycopy(paths, 0, totalPaths, 2, paths.length);
		return Paths.get("config", totalPaths).normalize().toAbsolutePath();
	}
	
	public final String getAbsolutePathString(String... paths)
	{
		return getAbsolutePath(paths).toString();
	}
	
	public final Path getRelativePath(String... paths)
	{
		final String[] totalPaths = new String[paths.length + 2];
		totalPaths[0] = "plugins";
		totalPaths[1] = getName();
		System.arraycopy(paths, 0, totalPaths, 2, paths.length);
		return Paths.get("config", totalPaths).normalize();
	}
	
	public final String getRelativePathString(String... paths)
	{
		return getRelativePath(paths).toString();
	}
	
	public int getPriority()
	{
		return 0;
	}
}
