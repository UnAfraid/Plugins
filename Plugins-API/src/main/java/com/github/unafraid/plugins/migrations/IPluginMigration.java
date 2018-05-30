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
package com.github.unafraid.plugins.migrations;

import com.github.unafraid.plugins.AbstractPlugin;

/**
 * An interface to be used to create plugin migrations.
 * @author UnAfraid
 */
public interface IPluginMigration {
	/**
	 * Gets the description of this migration.<br>
	 * In short: the reason of change, what happened, etc.
	 * @return description
	 */
	public String getDescription();
	
	/**
	 * The target version where migrations is applicable.
	 * @return target version
	 */
	public int getTargetVersion();
	
	/**
	 * Triggered whenever the plugin is being migrated.
	 * @param plugin the plugin
	 */
	public void migrate(AbstractPlugin plugin);
}
