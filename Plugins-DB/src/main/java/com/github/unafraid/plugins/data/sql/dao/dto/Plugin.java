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
package com.github.unafraid.plugins.data.sql.dao.dto;

/**
 * @author UnAfraid
 */
public class Plugin
{
	private final int id;
	private String name;
	private int version;
	
	/**
	 * @param id
	 * @param name
	 * @param version
	 */
	public Plugin(int id, String name, int version)
	{
		this.id = id;
		this.name = name;
		this.version = version;
	}
	
	/**
	 * @return the id
	 */
	public int getId()
	{
		return id;
	}
	
	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * @return the version
	 */
	public int getVersion()
	{
		return version;
	}
	
	/**
	 * @param version the version to set
	 */
	public void setVersion(int version)
	{
		this.version = version;
	}
}
