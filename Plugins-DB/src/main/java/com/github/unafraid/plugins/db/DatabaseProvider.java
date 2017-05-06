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

import org.skife.jdbi.v2.DBI;

import com.github.unafraid.plugins.util.ClassPathUtil;

/**
 * @author UnAfraid
 */
public class DatabaseProvider
{
	public static IDatabaseFactory DATABASE_FACTORY;
	public static DBI DBI;
	static
	{
		try
		{
			DATABASE_FACTORY = ClassPathUtil.getInstanceOfExtending(IDatabaseFactory.class);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		
		try
		{
			DBI = new DBI(DATABASE_FACTORY.getDataSource());
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private DatabaseProvider()
	{
	}
}
