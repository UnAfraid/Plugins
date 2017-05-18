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
package com.github.unafraid.plugins.db;

import org.skife.jdbi.v2.DBI;

import com.github.unafraid.plugins.db.dao.PluginsDAO;
import com.github.unafraid.plugins.util.ClassPathUtil;

/**
 * A class used to let the plugin API know what kind of Database Factory you use.
 * @author UnAfraid
 */
public class DatabaseProvider
{
	public static final IDatabaseFactory DATABASE_FACTORY;
	public static final DBI DBI;
	public static final PluginsDAO PLUGINS_DAO;
	
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
		
		try
		{
			PLUGINS_DAO = DBI.open(PluginsDAO.class);
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}
	
	private DatabaseProvider()
	{
		// Hide constructor.
	}
}
