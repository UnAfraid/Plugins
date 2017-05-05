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
package com.github.unafraid.plugins.data.sql.dao;

import java.io.Closeable;
import java.util.List;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

import com.github.unafraid.plugins.data.sql.dao.dto.Plugin;
import com.github.unafraid.plugins.data.sql.dao.mapper.PluginMapper;

/**
 * @author UnAfraid
 */
@RegisterMapper(PluginMapper.class)
public interface PluginsDAO extends Closeable
{
	@SqlUpdate("INSERT INTO `plugins`(`name`, `version`) VALUES (:name, :version)")
	@GetGeneratedKeys
	int insert(@Bind("name") String name, @Bind("version") int version);
	
	@SqlUpdate("DELETE FROM `plugins` WHERE `id` = :id")
	void delete(@Bind("id") int id);
	
	@SqlUpdate("DELETE FROM `plugins` WHERE `name` = :name")
	void deleteByName(@Bind("name") String name);
	
	@SqlQuery("SELECT * FROM `plugins` WHERE `name` = :name")
	Plugin findByName(@Bind("name") String name);
	
	@SqlQuery("SELECT * FROM `plugins`")
	List<Plugin> findAll();
	
	@Override
	void close();
}
