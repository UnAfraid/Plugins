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
package com.github.unafraid.plugins.db;

import java.sql.Connection;

import javax.sql.DataSource;

/**
 * A general database factory cross interface.
 * @author UnAfraid
 */
public interface IDatabaseFactory
{
	/**
	 * Gets the connection implementation of your database factory.
	 * @return connection
	 */
	Connection getConnection();
	
	/**
	 * Gets the data source implementation of your database factory.
	 * @return data source
	 */
	DataSource getDataSource();
	
	/**
	 * Shutdowns your database factory.
	 */
	void shutdown();
}
