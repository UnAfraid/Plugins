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
package com.github.unafraid.plugins.conditions;

/**
 * The condition result class.
 * @author UnAfraid
 */
public class ConditionResult
{
	private final boolean _isSuccess;
	private final String _description;
	
	/**
	 * Constructs a new condition result.
	 * @param isSuccess whether condition is successful or not
	 * @param description the description of the condition
	 */
	public ConditionResult(boolean isSuccess, String description)
	{
		_isSuccess = isSuccess;
		_description = description;
	}
	
	/**
	 * Whether condition is successful or not.
	 * @return {@code true} if condition is successful, otherwise {@code false}
	 */
	public boolean isSuccess()
	{
		return _isSuccess;
	}
	
	/**
	 * Describes the condition.
	 * @return description
	 */
	public String describe()
	{
		return _description;
	}
	
	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = (prime * result) + ((_description == null) ? 0 : _description.hashCode());
		result = (prime * result) + (_isSuccess ? 1231 : 1237);
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
		final ConditionResult other = (ConditionResult) obj;
		if (_description == null)
		{
			if (other._description != null)
			{
				return false;
			}
		}
		else if (!_description.equals(other._description))
		{
			return false;
		}
		if (_isSuccess != other._isSuccess)
		{
			return false;
		}
		return true;
	}
}