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

package com.rpkit.essentials.bukkit.logmessage

import com.rpkit.core.service.Service
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.table.RPKLogMessagesEnabledTable
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKLogMessageService(override val plugin: RPKEssentialsBukkit) : Service {

    fun isLogMessagesEnabled(minecraftProfile: RPKMinecraftProfile): CompletableFuture<Boolean> {
        return plugin.database.getTable(RPKLogMessagesEnabledTable::class.java)[minecraftProfile].thenApply { it != null }
    }

    fun setLogMessagesEnabled(minecraftProfile: RPKMinecraftProfile, enabled: Boolean): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val logMessagesEnabledTable = plugin.database.getTable(RPKLogMessagesEnabledTable::class.java)
            val logMessagesEnabled = logMessagesEnabledTable[minecraftProfile].join()
            if (logMessagesEnabled != null) {
                if (!enabled) {
                    logMessagesEnabledTable.delete(logMessagesEnabled).join()
                }
            } else {
                if (enabled) {
                    logMessagesEnabledTable.insert(RPKLogMessagesEnabled(minecraftProfile)).join()
                }
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to set log messages enabled", exception)
            throw exception
        }
    }

}