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
package com.github.unafraid.plugins.util;

import com.github.unafraid.plugins.exceptions.PluginRuntimeException;

/**
 * A simple wrapper class to invoke {@link PluginRuntimeException} in case an exception is caught.
 * @author UnAfraid
 */
public interface ThrowableRunnable extends Runnable
{
	@Override
	default void run()
	{
		try
		{
			runOrThrow();
		}
		catch (Throwable t)
		{
			throw new PluginRuntimeException(t);
		}
	}
	
	void runOrThrow() throws Throwable;
}
