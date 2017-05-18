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