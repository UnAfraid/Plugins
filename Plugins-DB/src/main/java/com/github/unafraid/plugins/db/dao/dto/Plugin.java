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
package com.github.unafraid.plugins.db.dao.dto;

import java.util.Date;

/**
 * A simple holder class storing plugin DB information.
 * @author UnAfraid
 */
public class Plugin
{
	private final int id;
	private String name;
	private int version;
	private long installedOn;
	
	/**
	 * @param id
	 * @param name
	 * @param version
	 * @param installedOn
	 */
	public Plugin(int id, String name, int version, long installedOn)
	{
		this.id = id;
		this.name = name;
		this.version = version;
		this.installedOn = installedOn;
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
	
	/**
	 * @return the installedOn
	 */
	public long getInstalledOn()
	{
		return installedOn;
	}
	
	/**
	 * @param installedOn the installedOn to set
	 */
	public void setInstalledOn(long installedOn)
	{
		this.installedOn = installedOn;
	}
	
	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		sb.append("ID: ").append(getId()).append(", ");
		sb.append("Name: ").append(getName()).append(", ");
		sb.append("Version: ").append(getVersion()).append(", ");
		sb.append("installedOn: ").append(new Date(getInstalledOn())).append(", ");
		// Soon ...
		// sb.append("state: ").append(PluginState.values()[getState()]);
		return sb.toString();
	}
}
