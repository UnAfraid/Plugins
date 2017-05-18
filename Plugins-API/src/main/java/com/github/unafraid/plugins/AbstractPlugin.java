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
import java.util.Objects;
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
 * This class is the parent class of all plugins.<br>
 * Contains several event hooks you may implement to your own plugin.<br>
 * Whenever you make a new plugin, please <br>
 * <b>do not forget to invoke {@link #init()} in the <u>constructor</u> of your plugin. Otherwise it will not work!</b>
 * @author UnAfraid
 */
public abstract class AbstractPlugin
{
	private final PluginConditions _conditions = new PluginConditions();
	private final FileInstaller _fileInstaller = new FileInstaller();
	private final PluginMigrations _migrations = new PluginMigrations();
	private final List<IPluginInstaller> _installers = new ArrayList<>(Collections.singleton(_fileInstaller));
	private final AtomicReference<PluginState> _state = new AtomicReference<>(PluginState.AVAILABLE);
	
	/**
	 * Gets the name of the plugin.<br>
	 * People often use {@link Class#getSimpleName()} but you are allowed to use your own naming also.
	 * @return plugin name
	 */
	public abstract String getName();
	
	/**
	 * Gets the name of the author.<br>
	 * Usage of {@link String#intern()} is recommend if you have multiple plugins under your name.
	 * @return author's name
	 */
	public abstract String getAuthor();
	
	/**
	 * Gets the creation date of this plugin.
	 * @return creation date
	 */
	public abstract String getCreatedAt();
	
	/**
	 * Gets the plugin's description.<br>
	 * Feel free to provide some short details of your plugin.
	 * @return description
	 */
	public abstract String getDescription();
	
	/**
	 * Gets the actual version of this plugin.
	 * @return version
	 */
	public abstract int getVersion();
	
	/**
	 * Triggered whenever the plugin is being initialized.
	 * @param fileInstaller the file installer
	 * @param migrations the relevant plugin migrations
	 * @param pluginConditions plugin conditions
	 */
	protected abstract void setup(FileInstaller fileInstaller, PluginMigrations migrations, PluginConditions pluginConditions);
	
	/**
	 * Triggered whenever you install your plugin.
	 */
	protected abstract void onInstall();
	
	/**
	 * Triggered whenever you uninstall your plugin.
	 */
	protected abstract void onUninstall();
	
	/**
	 * Triggered whenever you migrate your plugin from a version to another.
	 * @param from previous release
	 * @param to actual release
	 */
	protected abstract void onMigrate(int from, int to);
	
	/**
	 * Triggered whenever your plugin starts.
	 */
	protected abstract void onStart();
	
	/**
	 * Triggered whenever your plugin stops.
	 */
	protected abstract void onStop();
	
	/**
	 * Sets the state of the plugin.
	 * @param currentState state from
	 * @param newState state to
	 * @return {@code true} when the state is changed, otherwise {@code false}
	 */
	protected final boolean setState(PluginState currentState, PluginState newState)
	{
		Objects.requireNonNull(currentState);
		Objects.requireNonNull(newState);
		
		if (_state.compareAndSet(currentState, newState))
		{
			onStateChanged(currentState, newState);
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the actual state of the plugin.
	 * @return state
	 */
	public final PluginState getState()
	{
		return _state.get();
	}
	
	/**
	 * An event triggered whenever the plugin's state is changed.
	 * @param oldState the previous state
	 * @param newState the actual state
	 */
	public void onStateChanged(PluginState oldState, PluginState newState)
	{
		
	}
	
	/**
	 * A mandatory method that must be triggered in the constructor of your plugin.<br>
	 * Sets up the plugin's environment related things and changed the plugin's state from available to initialized.
	 */
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
	
	/**
	 * Starts your plugin or throws an exception.
	 * @throws PluginException
	 */
	public final void start() throws PluginException
	{
		_conditions.testConditions(ConditionType.START, this);
		verifyStateAndRun(this::onStart, PluginState.INSTALLED, PluginState.STARTED);
	}
	
	/**
	 * Stops your plugin or throws an exception.
	 * @throws PluginException
	 */
	public final void stop() throws PluginException
	{
		_conditions.testConditions(ConditionType.STOP, this);
		verifyStateAndRun(this::onStop, PluginState.STARTED, PluginState.INSTALLED);
	}
	
	/**
	 * Installs your plugin or throws an exception.
	 * @throws PluginException
	 */
	public final void install() throws PluginException
	{
		_conditions.testConditions(ConditionType.INSTALL, this);
		verifyStateAndRun(() ->
		{
			for (IPluginInstaller installer : _installers)
			{
				installer.install(this);
			}
			
			onInstall();
		}, PluginState.INITIALIZED, PluginState.INSTALLED);
	}
	
	/**
	 * Uninstalls your plugin or throws an exception.
	 * @throws PluginException
	 */
	public final void uninstall() throws PluginException
	{
		_conditions.testConditions(ConditionType.UNINSTALL, this);
		verifyStateAndRun(() ->
		{
			for (IPluginInstaller installer : _installers)
			{
				installer.uninstall(this);
			}
			
			onUninstall();
		}, PluginState.INSTALLED, PluginState.INITIALIZED);
	}
	
	/**
	 * Migrates your plugin from an older version to another.
	 * @param from the older version
	 * @param to the actual (newer) version
	 * @throws PluginException
	 */
	public final void migrate(int from, int to) throws PluginException
	{
		_conditions.testConditions(ConditionType.MIGRATION, this);
		verifyStateAndRun(() ->
		{
			_migrations.migrate(from, to, this);
			
			onMigrate(from, to);
		}, PluginState.INSTALLED, getState());
	}
	
	/**
	 * Verifies the state of the plugin.
	 * @param run a runnable wrapper triggered when set state was successful
	 * @param expectedState the expected state (from)
	 * @param newState the actual (newer) state (to)
	 * @throws PluginException
	 */
	private void verifyStateAndRun(ThrowableRunnable run, PluginState expectedState, PluginState newState) throws PluginException
	{
		Objects.requireNonNull(run);
		Objects.requireNonNull(expectedState);
		Objects.requireNonNull(newState);
		
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
	
	/**
	 * Gets the plugin condition holder.
	 * @return conditions
	 */
	public final PluginConditions getConditions()
	{
		return _conditions;
	}
	
	/**
	 * Gets the file installer.
	 * @return file installer
	 */
	public final FileInstaller getFileInstaller()
	{
		return _fileInstaller;
	}
	
	/**
	 * Gets a list of the plugin installers that implements {@link IPluginInstaller}.
	 * @return plugin installer list
	 */
	public List<IPluginInstaller> getInstallers()
	{
		return _installers;
	}
	
	/**
	 * Gets the plugin migrations storage.
	 * @return migrations
	 */
	public final PluginMigrations getMigrations()
	{
		return _migrations;
	}
	
	/**
	 * Gets the absolute path of the parameter.
	 * @param paths path parameters given by the user
	 * @return absolute path
	 */
	public final Path getAbsolutePath(String... paths)
	{
		final String[] totalPaths = new String[paths.length + 2];
		totalPaths[0] = "plugins";
		totalPaths[1] = getName();
		System.arraycopy(paths, 0, totalPaths, 2, paths.length);
		return Paths.get("config", totalPaths).normalize().toAbsolutePath();
	}
	
	/**
	 * Gets the string version of {@link #getAbsolutePath(String...)}, uses {@link Path#toString()}.
	 * @param paths
	 * @return absolute path
	 */
	public final String getAbsolutePathString(String... paths)
	{
		return getAbsolutePath(paths).toString();
	}
	
	/**
	 * Gets the relative path of the given parameters.
	 * @param paths path parameters given by the user
	 * @return relative path
	 */
	public final Path getRelativePath(String... paths)
	{
		final String[] totalPaths = new String[paths.length + 2];
		totalPaths[0] = "plugins";
		totalPaths[1] = getName();
		System.arraycopy(paths, 0, totalPaths, 2, paths.length);
		return Paths.get("config", totalPaths).normalize();
	}
	
	/**
	 * Gets the string version of {@link #getRelativePath(String...)}, uses {@link Path#toString()}.
	 * @param paths path given by the user
	 * @return relative path
	 */
	public final String getRelativePathString(String... paths)
	{
		return getRelativePath(paths).toString();
	}
	
	/**
	 * Gets plugin's priority.
	 * @return priority
	 */
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
