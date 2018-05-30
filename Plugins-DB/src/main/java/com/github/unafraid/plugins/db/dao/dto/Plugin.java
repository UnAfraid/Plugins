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
public class Plugin {
	private final int id;
	private final String name;
	private final int version;
	private final long installedOn;
	private final int autoStart;
	
	/**
	 * @param id
	 * @param name
	 * @param version
	 * @param installedOn
	 * @param autoStart
	 */
	public Plugin(int id, String name, int version, long installedOn, int autoStart) {
		this.id = id;
		this.name = name;
		this.version = version;
		this.installedOn = installedOn;
		this.autoStart = autoStart;
	}
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return the version
	 */
	public int getVersion() {
		return version;
	}
	
	/**
	 * @return the installedOn
	 */
	public long getInstalledOn() {
		return installedOn;
	}
	
	/**
	 * @return the autoStart
	 */
	public boolean isAutoStart() {
		return autoStart == 1;
	}
	
	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("ID: ").append(getId()).append(", ");
		sb.append("Name: ").append(getName()).append(", ");
		sb.append("Version: ").append(getVersion()).append(", ");
		sb.append("installedOn: ").append(new Date(getInstalledOn())).append(", ");
		sb.append("autoStart: ").append(isAutoStart());
		return sb.toString();
	}
}
