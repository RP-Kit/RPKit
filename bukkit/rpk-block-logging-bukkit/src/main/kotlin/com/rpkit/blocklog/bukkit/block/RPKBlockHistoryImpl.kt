/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.blocklog.bukkit.block

import com.rpkit.blocklog.bukkit.RPKBlockLoggingBukkit
import com.rpkit.blocklog.bukkit.database.table.RPKBlockChangeTable
import com.rpkit.blocklog.bukkit.database.table.RPKBlockInventoryChangeTable
import org.bukkit.World


class RPKBlockHistoryImpl(
        private val plugin: RPKBlockLoggingBukkit,
        override var id: Int? = null,
        override val world: World,
        override val x: Int,
        override val y: Int,
        override val z: Int
) : RPKBlockHistory {

    override val changes: List<RPKBlockChange>
        get() = plugin.database.getTable(RPKBlockChangeTable::class).get(this)

    override val inventoryChanges: List<RPKBlockInventoryChange>
        get() = plugin.database.getTable(RPKBlockInventoryChangeTable::class).get(this)

}