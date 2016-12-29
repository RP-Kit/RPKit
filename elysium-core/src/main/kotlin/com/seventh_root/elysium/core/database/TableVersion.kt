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

/**
 * Represents the version of a table.
 *
 * @property id The ID of the table version, defaults to 0. Set automatically when inserted into a table.
 * @property table The name of the table
 * @property version The version of the table
 */
class TableVersion(
        override var id: Int = 0,
        val table: String,
        var version: String
): Entity