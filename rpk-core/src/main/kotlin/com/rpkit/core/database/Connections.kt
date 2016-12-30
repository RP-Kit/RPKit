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

package com.rpkit.core.database

import java.sql.Connection

/**
 * Executes specified block with connection and close connection after this
 *
 * @param block the block to execute
 * @return The result of the block
 */
fun <T> Connection.use(block: (Connection) -> T): T {
    try {
        return block(this)
    } finally {
        this.close()
    }
}