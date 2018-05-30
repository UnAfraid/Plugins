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
package com.github.unafraid.plugins.installers.file;

import java.util.StringJoiner;

/**
 * A simple holder class that represents a plugin file.
 * @author UnAfraid
 */
public class PluginFile
{
	private final String source;
	private final String destination;
	
	/**
	 * Constructs the plugin file.
	 * @param source where installer should look for the original file
	 * @param destination where installer shall put the file
	 */
	public PluginFile(String source, String destination)
	{
		this.source = source;
		this.destination = destination;
	}
	
	/**
	 * Gets name of the original file.
	 * @return source file
	 */
	public String getSource()
	{
		return source;
	}
	
	/**
	 * Gets the name of the destination file.
	 * @return destination file
	 */
	public String getDestination()
	{
		return destination;
	}
	
	@Override
	public String toString()
	{
		final StringJoiner sj = new StringJoiner(", ", getClass().getSimpleName() + "[", "]");
		sj.add("source: " + source);
		sj.add("destination: " + destination);
		return sj.toString();
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((destination == null) ? 0 : destination.hashCode());
		result = (prime * result) + ((source == null) ? 0 : source.hashCode());
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
		final PluginFile other = (PluginFile) obj;
		if (destination == null)
		{
			if (other.destination != null)
			{
				return false;
			}
		}
		else if (!destination.equals(other.destination))
		{
			return false;
		}
		if (source == null)
		{
			if (other.source != null)
			{
				return false;
			}
		}
		else if (!source.equals(other.source))
		{
			return false;
		}
		return true;
	}
}
