package com.seventh_root.elysium.core.database


class TableVersion(override var id: Int, val table: String, var version: String): TableRow {
    constructor(table: String, version: String): this(0, table, version)
}