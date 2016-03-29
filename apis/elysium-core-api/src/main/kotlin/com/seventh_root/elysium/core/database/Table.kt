package com.seventh_root.elysium.core.database

import com.google.common.base.CaseFormat.LOWER_UNDERSCORE
import com.google.common.base.CaseFormat.UPPER_CAMEL

abstract class Table<T : TableRow>(val database: Database, val name: String, val type: Class<T>) {

    constructor(database: Database, type: Class<T>) : this(database, UPPER_CAMEL.to(LOWER_UNDERSCORE, type.simpleName), type) {
    }

    abstract fun create()

    open fun applyMigrations() {

    }

    abstract fun insert(`object`: T): Int

    abstract fun update(`object`: T)

    abstract operator fun get(id: Int): T?

    abstract fun delete(`object`: T)

}
