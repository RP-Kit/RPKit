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

package com.seventh_root.elysium.core.database

import com.google.common.base.CaseFormat.LOWER_UNDERSCORE
import com.google.common.base.CaseFormat.UPPER_CAMEL
import kotlin.reflect.KClass

/**
 * Represents a table.
 * These are managed by a [Database] instance.
 * Tables may manage their own caching internally.
 *
 * @property database The [Database] instance which manages the table
 * @property name The name of the table
 * @property type The type of entity stored in the table
 */
abstract class Table<T: Entity>(val database: Database, val name: String, val type: KClass<T>) {

    /**
     * Constructs a table with the given database and type.
     * The name is inferred from the name of the type.
     * The type is a [KClass] to facilitate easy usage from Kotlin. There is an equivalent constructor which uses Java's [Class].
     *
     * @param database The [Database] instance which manages the table
     * @param type The type of entity stored in the table
     */
    constructor(database: Database, type: KClass<T>): this(database, UPPER_CAMEL.to(LOWER_UNDERSCORE, type.simpleName), type)

    /**
     * Constructs a table with the given database, name and type.
     * The type is a [Class] to facilitate easy usage from Java. There is an equivalent constructor which uses Kotlin's [KClass].
     *
     * @param database The [Database] instance which manages this table
     * @param name The name of the table
     * @param type The type of entity stored in the table
     */
    constructor(database: Database, name: String, type: Class<T>): this(database, name, type.kotlin)

    /**
     * Constructs a table with the given database and type.
     * The name is inferred from the name of the type.
     * The type is a [Class] to facilitate easy usage from Java. There is an equivalent constructor which uses Kotlin's [KClass].
     *
     * @param database The [Database] instance which manages the table
     * @param type The type of entity stored in the table
     */
    constructor(database: Database, type: Class<T>): this(database, UPPER_CAMEL.to(LOWER_UNDERSCORE, type.simpleName), type.kotlin)

    /**
     * Creates the table on the database.
     * This should do any checking whether the table already exists (e.g. with an SQL 'IF NOT EXISTS'),
     * as this will be called on startup regardless of whether the table exists or not.
     */
    abstract fun create()

    /**
     * Applies any migrations if required, bringing this table to the version currently running on the server.
     * To check the table's current version on the server, use [Database.getTableVersion]
     */
    abstract fun applyMigrations()

    /**
     * Inserts an entity into the table, setting it's ID.
     *
     * @return The ID of the entity
     */
    abstract fun insert(entity: T): Int

    /**
     * Updates an entity in the table.
     *
     * @param entity The entity to update
     */
    abstract fun update(entity: T)

    /**
     * Gets an entity by ID.
     * If no entity is found with the given ID, null is returned.
     *
     * @return The entity, or null if none is found with the given ID
     */
    abstract operator fun get(id: Int): T?

    /**
     * Deletes the given entity from the table.
     *
     * @param entity The entity to delete
     */
    abstract fun delete(entity: T)

}
