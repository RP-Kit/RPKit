package com.seventh_root.elysium.core.database

import java.sql.Statement

fun <T, S: Statement> S.use(block: (S) -> T): T {
    try {
        return block(this)
    } finally {
        close()
    }
}