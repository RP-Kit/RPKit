/*
 * Copyright 2016 Ross Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.core.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.TableVersion
import com.rpkit.core.database.jooq.rpkit.Tables.TABLE_VERSION
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType

/**
 * Represents the database table used to record versions of tables.
 * Allows for migrations to be applied if a table is out of date.
 */
class TableVersionTable(database: Database): Table<TableVersion>(database, TableVersion::class.java) {

    override fun create() {
        database.create
                .createTableIfNotExists(TABLE_VERSION)
                .column(TABLE_VERSION.ID, SQLDataType.INTEGER.identity(true))
                .column(TABLE_VERSION.TABLE_NAME, SQLDataType.VARCHAR(256))
                .column(TABLE_VERSION.VERSION, SQLDataType.VARCHAR(32))
                .constraints(
                        constraint("pk_table_version").primaryKey(TABLE_VERSION.ID)
                )
                .execute()
        SQLiteDataType.INTEGER
    }

    override fun applyMigrations() {

    }

    override fun insert(entity: TableVersion): Int {
        database.create
                .insertInto(
                        TABLE_VERSION,
                        TABLE_VERSION.TABLE_NAME,
                        TABLE_VERSION.VERSION
                )
                .values(
                        entity.table,
                        entity.version
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        return id
    }

    override fun update(entity: TableVersion) {
        database.create
                .update(TABLE_VERSION)
                .set(TABLE_VERSION.TABLE_NAME, entity.table)
                .set(TABLE_VERSION.VERSION, entity.version)
                .where(TABLE_VERSION.ID.eq(entity.id))
                .execute()
    }

    override fun get(id: Int): TableVersion? {
        val result = database.create
                .select(
                        TABLE_VERSION.TABLE_NAME,
                        TABLE_VERSION.VERSION
                )
                .from(TABLE_VERSION)
                .where(TABLE_VERSION.ID.eq(id))
                .fetchOne() ?: return null
        val tableVersion = TableVersion(
                id,
                result.get(TABLE_VERSION.TABLE_NAME),
                result.get(TABLE_VERSION.VERSION)
        )
        return tableVersion
    }

    /**
     * Gets the version of a table.
     */
    fun get(table: String): TableVersion? {
        val result = database.create
                .select(
                        TABLE_VERSION.ID,
                        TABLE_VERSION.VERSION
                )
                .from(TABLE_VERSION)
                .where(TABLE_VERSION.TABLE_NAME.eq(table))
                .fetchOne() ?: return null
        val tableVersion = TableVersion(
                result.get(TABLE_VERSION.ID),
                table,
                result.get(TABLE_VERSION.VERSION)
        )
        return tableVersion
    }

    override fun delete(entity: TableVersion) {
        database.create
                .deleteFrom(TABLE_VERSION)
                .where(TABLE_VERSION.ID.eq(entity.id))
                .execute()
    }

}