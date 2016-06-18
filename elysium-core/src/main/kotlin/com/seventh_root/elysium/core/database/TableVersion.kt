package com.seventh_root.elysium.core.database


class TableVersion(
        override var id: Int = 0,
        val table: String,
        var version: String
): TableRow