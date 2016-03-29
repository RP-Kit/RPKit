package com.seventh_root.elysium.core.database

import java.sql.Connection

fun <T> Connection.use(block : (Connection) -> T) : T {
    try {
        return block(this)
    } finally {
        this.close()
    }
}