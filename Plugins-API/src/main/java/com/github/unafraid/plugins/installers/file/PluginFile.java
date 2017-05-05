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
package com.github.unafraid.plugins.installers.file;

import java.util.StringJoiner;

/**
 * @author UnAfraid
 */
public class PluginFile
{
	private final String _source;
	private final String _destination;
	
	public PluginFile(String source, String destination)
	{
		_source = source;
		_destination = destination;
	}
	
	public String getSource()
	{
		return _source;
	}
	
	public String getDestination()
	{
		return _destination;
	}
	
	@Override
	public String toString()
	{
		final StringJoiner sj = new StringJoiner(", ", getClass().getSimpleName() + "[", "]");
		sj.add("source" + _source);
		sj.add("destination: " + _destination);
		return sj.toString();
	}
}
