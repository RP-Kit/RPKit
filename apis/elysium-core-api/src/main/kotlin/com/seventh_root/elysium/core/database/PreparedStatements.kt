package com.seventh_root.elysium.core.database

import java.sql.PreparedStatement
import java.sql.ResultSet

fun PreparedStatement.update(): Int {
    try {
        return this.executeUpdate()
    } finally {
        close()
    }
}

fun <T> PreparedStatement.query(block: (ResultSet) -> T): T {
    try {
        val resultSet = this.executeQuery()
        return block(resultSet)
    } finally {
        close()
    }
}
