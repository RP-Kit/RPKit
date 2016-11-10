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

import java.sql.PreparedStatement
import java.sql.ResultSet

/**
 * Executes an update with the [PreparedStatement] and closes the statement after this
 *
 * @return either (1) the row count for SQL Data Manipulation Language (DML) statements or (2) 0 for SQL statements that return nothing
 */
fun PreparedStatement.update(): Int {
    try {
        return this.executeUpdate()
    } finally {
        close()
    }
}

/**
 * Executes a query with the [PreparedStatement] and closes the statement after this
 *
 * @param block The block to execute
 * @return The result of the block
 */
fun <T> PreparedStatement.query(block: (ResultSet) -> T): T {
    try {
        val resultSet = this.executeQuery()
        return block(resultSet)
    } finally {
        close()
    }
}
