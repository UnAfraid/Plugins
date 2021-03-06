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
package com.github.unafraid.plugins.db.dao;

import java.io.Closeable;
import java.util.List;

import com.github.unafraid.plugins.db.dao.dto.Plugin;
import com.github.unafraid.plugins.db.dao.mapper.PluginMapper;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;

/**
 * The plugin DAO. Stores DB queries.
 * @author UnAfraid
 */
@RegisterMapper(PluginMapper.class)
public interface PluginsDAO extends Closeable {
	@SqlUpdate("INSERT INTO `plugins`(`name`, `version`, `installedOn`, `autoStart`) VALUES (:name, :version, :installedOn, :autoStart)")
	@GetGeneratedKeys
	int insert(@Bind("name") String name, @Bind("version") int version, @Bind("installedOn") long installedOn, @Bind("autoStart") int autoStart);
	
	@SqlUpdate("UPDATE `plugins` SET `autoStart` = :autoStart WHERE `name` = :name")
	void updateAutoStartByName(@Bind("autoStart") int autoStart, @Bind("name") String name);
	
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
