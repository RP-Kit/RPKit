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

abstract class Table<T: Entity>(val database: Database, val name: String, val type: KClass<T>) {

    constructor(database: Database, type: KClass<T>): this(database, UPPER_CAMEL.to(LOWER_UNDERSCORE, type.simpleName), type)

    constructor(database: Database, name: String, type: Class<T>): this(database, name, type.kotlin)

    constructor(database: Database, type: Class<T>): this(database, UPPER_CAMEL.to(LOWER_UNDERSCORE, type.simpleName), type.kotlin)

    abstract fun create()

    abstract fun applyMigrations()

    abstract fun insert(entity: T): Int

    abstract fun update(entity: T)

    abstract operator fun get(id: Int): T?

    abstract fun delete(entity: T)

}
