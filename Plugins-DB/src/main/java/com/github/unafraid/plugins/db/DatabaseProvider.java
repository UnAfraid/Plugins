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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * A class used to let the plugin API know what kind of Database Factory you use.
 * @author UnAfraid
 */
public class DatabaseProvider
{
	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseProvider.class);
	private static final String TABLE_NAME = "plugins";
	public static final IDatabaseFactory DATABASE_FACTORY;
	public static final DBI DBI;
	
	static
	{
		try
		{
			final ArrayList<IDatabaseFactory> availableDatabaseFactories = Lists.newArrayList(ServiceLoader.load(IDatabaseFactory.class));
			
			if (availableDatabaseFactories.size() != 1)
			{
				throw new IllegalStateException("Invalid amount of provided database factories found: " + availableDatabaseFactories);
			}
			
			DATABASE_FACTORY = availableDatabaseFactories.get(0);
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
		
		try (Connection con = DATABASE_FACTORY.getConnection();
			ResultSet rs = con.getMetaData().getTables(null, null, TABLE_NAME, null))
		{
			boolean hasTable = false;
			while (rs.next())
			{
				final String tableName = rs.getString("TABLE_NAME");
				if ((tableName != null) && TABLE_NAME.equals(tableName))
				{
					hasTable = true;
					break;
				}
			}
			
			if (!hasTable)
			{
				try (Statement st = con.createStatement();
					BufferedReader reader = new BufferedReader(new InputStreamReader(DatabaseProvider.class.getResourceAsStream("/sql/plugins.sql"), StandardCharsets.UTF_8)))
				{
					final String sql = reader.lines().collect(Collectors.joining(System.lineSeparator()));
					st.execute(sql);
					LOGGER.info("Automatically installing table {}", TABLE_NAME);
				}
				catch (SQLException e)
				{
					LOGGER.warn("Failed to install table {} :", TABLE_NAME, e);
					throw new RuntimeException(e);
				}
			}
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
