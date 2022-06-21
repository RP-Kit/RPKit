/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.essentials.bukkit.tracking

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.table.RPKTrackingDisabledTable
import com.rpkit.tracking.bukkit.tracking.RPKTrackingService
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKTrackingServiceImpl(override val plugin: RPKEssentialsBukkit) : RPKTrackingService {

    override fun isTrackable(character: RPKCharacter): CompletableFuture<Boolean> {
        return plugin.database.getTable(RPKTrackingDisabledTable::class.java)[character].thenApply { it == null }
    }

    override fun setTrackable(character: RPKCharacter, trackable: Boolean): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val trackingDisabledTable = plugin.database.getTable(RPKTrackingDisabledTable::class.java)
            val trackingDisabled = trackingDisabledTable[character].join()
            if (trackingDisabled != null) {
                if (trackable) {
                    trackingDisabledTable.delete(trackingDisabled).join()
                }
            } else {
                if (!trackable) {
                    trackingDisabledTable.insert(RPKTrackingDisabled(character)).join()
                }
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to set trackable", exception)
            throw exception
        }
    }

}