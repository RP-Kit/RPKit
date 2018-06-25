/*
 * Copyright 2018 Ross Binden
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

package com.rpkit.moderation.bukkit.warning

import com.rpkit.moderation.bukkit.RPKModerationBukkit
import com.rpkit.moderation.bukkit.database.table.RPKWarningTable
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfile


class RPKWarningProviderImpl(private val plugin: RPKModerationBukkit): RPKWarningProvider {

    override fun getWarning(id: Int): RPKWarning? {
        return plugin.core.database.getTable(RPKWarningTable::class)[id]
    }

    override fun getWarnings(profile: RPKProfile): List<RPKWarning> {
        return plugin.core.database.getTable(RPKWarningTable::class).get(profile)
    }

    override fun addWarning(warning: RPKWarning) {
        val warningTable = plugin.core.database.getTable(RPKWarningTable::class)
        warningTable.insert(warning)
        // After adding warnings we want to execute any commands for that amount of warnings
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        for (minecraftProfile in minecraftProfileProvider.getMinecraftProfiles(warning.profile)) {
            val command = plugin.config.getString("warnings.${warningTable.get(warning.profile).size}")?.replace("\$player", minecraftProfile.minecraftUsername)
            if (command != null) {
                plugin.server.dispatchCommand(plugin.server.consoleSender, command)
            }
        }
    }

    override fun removeWarning(warning: RPKWarning) {
        plugin.core.database.getTable(RPKWarningTable::class).delete(warning)
    }

}