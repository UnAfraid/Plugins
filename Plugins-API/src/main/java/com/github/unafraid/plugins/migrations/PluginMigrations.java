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
public class PluginMigrations {
	private final Set<IPluginMigration> migrations = new HashSet<>();
	
	/**
	 * Registers a migration into this storage class.
	 * @param migration
	 */
	public void addMigration(IPluginMigration migration) {
		migrations.add(migration);
	}
	
	/**
	 * Gets a {@link Set} view of the migrations.
	 * @return migrations
	 */
	public Set<IPluginMigration> getMigrations() {
		return migrations;
	}
	
	/**
	 * A method to start all possible applicable migrations.
	 * @param from the older version
	 * @param to the newer version
	 * @param plugin the plugin
	 * @throws PluginException
	 */
	public void migrate(int from, int to, AbstractPlugin plugin) throws PluginException {
		if (from >= to) {
			throw new PluginException("Cannot migrate from " + from + " >= to" + to + "!");
		}
		
		//@formatter:off
		migrations.stream()
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
		result = (prime * result) + ((migrations == null) ? 0 : migrations.hashCode());
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
		if (migrations == null)
		{
			if (other.migrations != null)
			{
				return false;
			}
		}
		else if (!migrations.equals(other.migrations))
		{
			return false;
		}
		return true;
	}
}
