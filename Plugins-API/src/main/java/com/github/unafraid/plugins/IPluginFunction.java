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

import com.github.unafraid.plugins.exceptions.PluginException;

/**
 * @author UnAfraid
 * @param <T>
 */
@SuppressWarnings("unused")
public interface IPluginFunction<T extends AbstractPlugin> {
	/**
	 * Triggered whenever you install your plugin.
	 * @throws PluginException
	 */
	default void onInstall() throws PluginException {
		// to be overridden
	}
	
	/**
	 * Triggered whenever you uninstall your plugin.
	 * @throws PluginException
	 */
	default void onUninstall() throws PluginException {
		// to be overridden
	}
	
	/**
	 * Triggered whenever you migrate your plugin from a version to another.
	 * @param from previous release
	 * @param to actual release
	 * @throws PluginException
	 */
	default void onMigrate(int from, int to) throws PluginException {
		// to be overridden
	}
	
	/**
	 * Triggered whenever your plugin starts.
	 * @throws PluginException
	 */
	abstract void onStart() throws PluginException;
	
	/**
	 * Triggered whenever your plugin stops.
	 * @throws PluginException
	 */
	abstract void onStop() throws PluginException;
	
	/**
	 * Triggered whenever you reload the plugin.
	 * @throws PluginException
	 */
	default void onReload() throws PluginException {
		// to be overridden
	}
	
	/**
	 * Gets the plugin owner of this function
	 * @return the plugin itself
	 */
	public T getPlugin();
}
