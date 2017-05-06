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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.github.unafraid.plugins.conditions.ConditionType;
import com.github.unafraid.plugins.conditions.PluginConditions;
import com.github.unafraid.plugins.exceptions.PluginException;
import com.github.unafraid.plugins.exceptions.PluginRuntimeException;
import com.github.unafraid.plugins.installers.IPluginInstaller;
import com.github.unafraid.plugins.installers.file.FileInstaller;
import com.github.unafraid.plugins.migrations.PluginMigrations;
import com.github.unafraid.plugins.util.ThrowableRunnable;

/**
 * @author UnAfraid
 */
public abstract class AbstractPlugin
{
	private final PluginConditions _conditions = new PluginConditions();
	private final FileInstaller _fileInstaller = new FileInstaller();
	private final PluginMigrations _migrations = new PluginMigrations();
	private final List<IPluginInstaller> _installers = new ArrayList<>(Collections.singleton(_fileInstaller));
	private final AtomicReference<PluginState> _state = new AtomicReference<>(PluginState.AVAILABLE);
	
	public abstract String getName();
	
	public abstract String getAuthor();
	
	public abstract String getCreatedAt();
	
	public abstract String getDescription();
	
	public abstract int getVersion();
	
	protected abstract void setup(FileInstaller fileInstaller, PluginMigrations migrations, PluginConditions pluginConditions);
	
	protected abstract void onStart();
	
	protected abstract void onStop();
	
	public final boolean setState(PluginState currentState, PluginState newState)
	{
		if (_state.compareAndSet(currentState, newState))
		{
			onStateChanged(currentState, newState);
			return true;
		}
		return false;
	}
	
	public final PluginState getState()
	{
		return _state.get();
	}
	
	public void onStateChanged(PluginState oldState, PluginState newState)
	{
		
	}
	
	protected final void init()
	{
		try
		{
			verifyStateAndRun(() -> setup(_fileInstaller, _migrations, _conditions), PluginState.AVAILABLE, PluginState.INITIALIZED);
		}
		catch (PluginException e)
		{
			throw new PluginRuntimeException(e);
		}
	}
	
	public final void start() throws PluginException
	{
		_conditions.testConditions(ConditionType.START, this);
		verifyStateAndRun(this::onStart, PluginState.INSTALLED, PluginState.STARTED);
	}
	
	public final void stop() throws PluginException
	{
		_conditions.testConditions(ConditionType.STOP, this);
		verifyStateAndRun(this::onStop, PluginState.STARTED, PluginState.INSTALLED);
	}
	
	public final void install() throws PluginException
	{
		_conditions.testConditions(ConditionType.INSTALL, this);
		verifyStateAndRun(() ->
		{
			for (IPluginInstaller installer : _installers)
			{
				installer.install(this);
			}
		}, PluginState.INITIALIZED, PluginState.INSTALLED);
	}
	
	public final void uninstall() throws PluginException
	{
		_conditions.testConditions(ConditionType.UNINSTALL, this);
		verifyStateAndRun(() ->
		{
			for (IPluginInstaller installer : _installers)
			{
				installer.uninstall(this);
			}
		}, PluginState.INSTALLED, PluginState.INITIALIZED);
	}
	
	public final void migrate(int from, int to) throws PluginException
	{
		_conditions.testConditions(ConditionType.MIGRATION, this);
		verifyStateAndRun(() -> _migrations.migrate(from, to, this), PluginState.INSTALLED, getState());
	}
	
	private void verifyStateAndRun(ThrowableRunnable run, PluginState expectedState, PluginState newState) throws PluginException
	{
		final PluginState currentState = getState();
		if (expectedState == currentState)
		{
			if (setState(currentState, newState))
			{
				run.run();
				return;
			}
			throw new PluginException("Failed to set state expected " + expectedState + " but got changed suddenly to " + getState());
		}
		
		throw new PluginException("Plugin proceed, expected state " + expectedState + " but found " + currentState);
	}
	
	public final PluginConditions getConditions()
	{
		return _conditions;
	}
	
	public final FileInstaller getFileInstaller()
	{
		return _fileInstaller;
	}
	
	public List<IPluginInstaller> getInstallers()
	{
		return _installers;
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
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((_conditions == null) ? 0 : _conditions.hashCode());
		result = (prime * result) + ((_installers == null) ? 0 : _installers.hashCode());
		result = (prime * result) + ((_fileInstaller == null) ? 0 : _fileInstaller.hashCode());
		result = (prime * result) + ((_migrations == null) ? 0 : _migrations.hashCode());
		result = (prime * result) + ((_state == null) ? 0 : _state.hashCode());
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
		final AbstractPlugin other = (AbstractPlugin) obj;
		if (_conditions == null)
		{
			if (other._conditions != null)
			{
				return false;
			}
		}
		else if (!_conditions.equals(other._conditions))
		{
			return false;
		}
		if (_installers == null)
		{
			if (other._installers != null)
			{
				return false;
			}
		}
		else if (!_installers.equals(other._installers))
		{
			return false;
		}
		if (_fileInstaller == null)
		{
			if (other._fileInstaller != null)
			{
				return false;
			}
		}
		else if (!_fileInstaller.equals(other._fileInstaller))
		{
			return false;
		}
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
		if (_state == null)
		{
			if (other._state != null)
			{
				return false;
			}
		}
		else if (!_state.equals(other._state))
		{
			return false;
		}
		return true;
	}
}
