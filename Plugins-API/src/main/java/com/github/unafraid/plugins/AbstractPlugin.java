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

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import com.github.unafraid.plugins.conditions.ConditionType;
import com.github.unafraid.plugins.conditions.PluginConditions;
import com.github.unafraid.plugins.exceptions.PluginException;
import com.github.unafraid.plugins.exceptions.PluginRuntimeException;
import com.github.unafraid.plugins.installers.IPluginInstaller;
import com.github.unafraid.plugins.installers.file.FileInstaller;
import com.github.unafraid.plugins.migrations.PluginMigrations;
import com.github.unafraid.plugins.util.ThrowableRunnable;
import com.google.common.base.MoreObjects;

/**
 * This class is the parent class of all plugins.<br>
 * Contains several event hooks you may implement to your own plugin.<br>
 * Whenever you make a new plugin, please <br>
 * <b>do not forget to invoke {@link #init()} in the <u>constructor</u> of your plugin. Otherwise it will not work!</b>
 * @author UnAfraid
 */
public abstract class AbstractPlugin {
	private final PluginConditions conditions = new PluginConditions();
	private final FileInstaller fileInstaller = new FileInstaller();
	private final PluginMigrations migrations = new PluginMigrations();
	private final List<IPluginInstaller> installers = new ArrayList<>(Collections.singleton(fileInstaller));
	private final AtomicReference<PluginState> state = new AtomicReference<>(PluginState.AVAILABLE);
	private final Set<IPluginFunction<? extends AbstractPlugin>> functions = new LinkedHashSet<>();
	private Path jarPath;
	private String jarHash;
	
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
	 * Gets an instance of a function that corresponds to the function class given
	 * @param <T>
	 * @param <R>
	 * @param functionClass
	 * @return an instance of a function
	 */
	public final <T extends AbstractPlugin, R extends IPluginFunction<T>> R getFunction(Class<R> functionClass) {
		return functions.stream().filter(function -> functionClass.isInstance(function)).map(functionClass::cast).findFirst().orElse(null);
	}
	
	/**
	 * Registers function of an plugin
	 * @param <T> the generic plugin type
	 * @param function the function of the plugin
	 */
	protected <T extends AbstractPlugin> void registerFunction(IPluginFunction<T> function) {
		functions.add(function);
	}
	
	/**
	 * Triggered whenever the plugin is being initialized.
	 * @param fileInstaller the file installer
	 * @param migrations the relevant plugin migrations
	 * @param pluginConditions plugin conditions
	 */
	protected abstract void setup(FileInstaller fileInstaller, PluginMigrations migrations, PluginConditions pluginConditions);
	
	/**
	 * Sets the state of the plugin.
	 * @param currentState state from
	 * @param newState state to
	 * @return {@code true} when the state is changed, otherwise {@code false}
	 */
	public final boolean setState(PluginState currentState, PluginState newState) {
		Objects.requireNonNull(currentState);
		Objects.requireNonNull(newState);
		
		if (state.compareAndSet(currentState, newState)) {
			onStateChanged(currentState, newState);
			return true;
		}
		return false;
	}
	
	/**
	 * Gets the actual state of the plugin.
	 * @return state
	 */
	public final PluginState getState() {
		return state.get();
	}
	
	/**
	 * An event triggered whenever the plugin's state is changed.
	 * @param oldState the previous state
	 * @param newState the actual state
	 */
	public void onStateChanged(PluginState oldState, PluginState newState) {
		// to be overridden
	}
	
	/**
	 * A mandatory method that must be triggered in the constructor of your plugin.<br>
	 * Sets up the plugin's environment related things and changed the plugin's state from available to initialized.
	 */
	protected final void init() {
		try {
			verifyStateAndRun(() -> setup(fileInstaller, migrations, conditions), PluginState.AVAILABLE, PluginState.INITIALIZED);
		}
		catch (PluginException e) {
			throw new PluginRuntimeException(e);
		}
	}
	
	/**
	 * Starts your plugin or throws an exception.
	 * @throws PluginException
	 */
	public final void start() throws PluginException {
		conditions.testConditions(ConditionType.START, this);
		verifyStateAndRun(() ->
		{
			for (IPluginInstaller installer : installers) {
				installer.repair(this);
			}
			
			for (IPluginFunction<?> function : functions) {
				function.onStart();
			}
		}, PluginState.INSTALLED, PluginState.STARTED);
	}
	
	/**
	 * Stops your plugin or throws an exception.
	 * @throws PluginException
	 */
	public final void stop() throws PluginException {
		conditions.testConditions(ConditionType.STOP, this);
		verifyStateAndRun(() ->
		{
			for (IPluginFunction<?> function : functions) {
				function.onStop();
			}
		}, PluginState.STARTED, PluginState.INSTALLED);
	}
	
	/**
	 * Reloads your plugin or throws an exception.
	 * @throws PluginException
	 */
	public final void reload() throws PluginException {
		conditions.testConditions(ConditionType.RELOAD, this);
		for (IPluginFunction<?> function : functions) {
			function.onReload();
		}
	}
	
	/**
	 * Installs your plugin or throws an exception.
	 * @throws PluginException
	 */
	public final void install() throws PluginException {
		conditions.testConditions(ConditionType.INSTALL, this);
		verifyStateAndRun(() ->
		{
			for (IPluginInstaller installer : installers) {
				installer.install(this);
			}
			
			for (IPluginFunction<?> function : functions) {
				function.onInstall();
			}
		}, PluginState.INITIALIZED, PluginState.INSTALLED);
	}
	
	/**
	 * Uninstalls your plugin or throws an exception.
	 * @throws PluginException
	 */
	public final void uninstall() throws PluginException {
		conditions.testConditions(ConditionType.UNINSTALL, this);
		verifyStateAndRun(() ->
		{
			for (IPluginInstaller installer : installers) {
				installer.uninstall(this);
			}
			
			for (IPluginFunction<?> function : functions) {
				function.onUninstall();
			}
		}, PluginState.INSTALLED, PluginState.INITIALIZED);
	}
	
	/**
	 * Migrates your plugin from an older version to another.
	 * @param from the older version
	 * @param to the actual (newer) version
	 * @throws PluginException
	 */
	public final void migrate(int from, int to) throws PluginException {
		conditions.testConditions(ConditionType.MIGRATION, this);
		verifyStateAndRun(() ->
		{
			migrations.migrate(from, to, this);
			
			for (IPluginFunction<?> function : functions) {
				function.onMigrate(from, to);
			}
		}, PluginState.INSTALLED, getState());
	}
	
	/**
	 * Verifies the state of the plugin.
	 * @param run a runnable wrapper triggered when set state was successful
	 * @param expectedState the expected state (from)
	 * @param newState the actual (newer) state (to)
	 * @throws PluginException
	 */
	private void verifyStateAndRun(ThrowableRunnable run, PluginState expectedState, PluginState newState) throws PluginException {
		Objects.requireNonNull(run);
		Objects.requireNonNull(expectedState);
		Objects.requireNonNull(newState);
		
		final PluginState currentState = getState();
		if (expectedState == currentState) {
			if (setState(currentState, newState)) {
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
	public final PluginConditions getConditions() {
		return conditions;
	}
	
	/**
	 * Gets the file installer.
	 * @return file installer
	 */
	public final FileInstaller getFileInstaller() {
		return fileInstaller;
	}
	
	/**
	 * Gets a list of the plugin installers that implements {@link IPluginInstaller}.
	 * @return plugin installer list
	 */
	public final List<IPluginInstaller> getInstallers() {
		return installers;
	}
	
	/**
	 * Gets the plugin migrations storage.
	 * @return migrations
	 */
	public final PluginMigrations getMigrations() {
		return migrations;
	}
	
	final void setJarPath(Path jarPath) {
		this.jarPath = jarPath;
	}
	
	public final Path getJarPath() {
		return jarPath;
	}
	
	final void setJarHash(String jarHash) {
		this.jarHash = jarHash;
	}
	
	public final String getJarHash() {
		return jarHash;
	}
	
	/**
	 * Normalises the path of the plugin.
	 * @param paths path parameters given by the user
	 * @return normalised path
	 */
	private Path getNormalizedPath(String... paths) {
		final String[] totalPaths = new String[paths.length + 1];
		totalPaths[0] = getName();
		System.arraycopy(paths, 0, totalPaths, 1, paths.length);
		return Paths.get("plugins", totalPaths).normalize();
	}
	
	/**
	 * Gets the absolute path of the parameter.
	 * @param paths path parameters given by the user
	 * @return absolute path
	 */
	public final Path getAbsolutePath(String... paths) {
		return getNormalizedPath(paths).toAbsolutePath();
	}
	
	/**
	 * Gets the string version of {@link #getAbsolutePath(String...)}, uses {@link Path#toString()}.
	 * @param paths
	 * @return absolute path
	 */
	public final String getAbsolutePathString(String... paths) {
		return getAbsolutePath(paths).toString();
	}
	
	/**
	 * Gets the relative path of the given parameters.
	 * @param paths path parameters given by the user
	 * @return relative path
	 */
	public final Path getRelativePath(String... paths) {
		return getNormalizedPath(paths);
	}
	
	/**
	 * Gets the string version of {@link #getRelativePath(String...)}, uses {@link Path#toString()}.
	 * @param paths path given by the user
	 * @return relative path
	 */
	public final String getRelativePathString(String... paths) {
		return getRelativePath(paths).toString();
	}
	
	/**
	 * Gets plugin's priority.
	 * @return priority
	 */
	public int getPriority() {
		return 0;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((getName() == null) ? 0 : getName().hashCode());
		result = (prime * result) + ((getAuthor() == null) ? 0 : getAuthor().hashCode());
		result = (prime * result) + ((getCreatedAt() == null) ? 0 : getCreatedAt().hashCode());
		result = (prime * result) + ((getDescription() == null) ? 0 : getDescription().hashCode());
		result = (prime * result) + ((getJarHash() == null) ? 0 : getJarHash().hashCode());
		result = (prime * result) + getVersion();
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		
		AbstractPlugin other = (AbstractPlugin) obj;
		if (!Objects.equals(getName(), other.getName())) {
			return false;
		}
		if (!Objects.equals(getAuthor(), other.getAuthor())) {
			return false;
		}
		if (!Objects.equals(getCreatedAt(), other.getCreatedAt())) {
			return false;
		}
		if (!Objects.equals(getDescription(), other.getDescription())) {
			return false;
		}
		if (!Objects.equals(getJarHash(), other.getJarHash())) {
			return false;
		}
		if (getVersion() != other.getVersion()) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public String toString() {
		return MoreObjects.toStringHelper(this)
			.add("name", getName())
			.add("author", getAuthor())
			.add("createdAt", getCreatedAt())
			.add("description", getDescription())
			.add("jarHash", getJarHash())
			.add("version", getVersion())
			.toString();
	}
}
