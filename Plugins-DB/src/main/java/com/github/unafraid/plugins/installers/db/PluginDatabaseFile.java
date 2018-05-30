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
package com.github.unafraid.plugins.installers.db;

import java.util.Optional;

/**
 * A simple holder class that stores a database file of the plugin.
 * @author UnAfraid
 */
public class PluginDatabaseFile {
	private final String _source;
	private final Optional<String> _name;
	private final Optional<String> _database;
	
	/**
	 * Constructs the database file of the plugin.
	 * @param source the SQL file the installer should seek for
	 * @param name the name of the table
	 * @param database the name of the database
	 */
	public PluginDatabaseFile(String source, Optional<String> name, Optional<String> database) {
		_source = source;
		_name = name;
		_database = database;
	}
	
	/**
	 * Gets the name of the source SQL file.
	 * @return source file name
	 */
	public String getSource() {
		return _source;
	}
	
	/**
	 * Gets the name of the table.
	 * @return table name
	 */
	public Optional<String> getName() {
		return _name;
	}
	
	/**
	 * Gets the name of the database.
	 * @return database name.
	 */
	public Optional<String> getDatabase() {
		return _database;
	}
}