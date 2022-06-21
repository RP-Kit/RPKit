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

package com.rpkit.featureflags.bukkit.featureflag

import com.rpkit.featureflags.bukkit.RPKFeatureFlagsBukkit
import com.rpkit.featureflags.bukkit.database.table.RPKProfileFeatureFlagTable
import com.rpkit.featureflags.bukkit.event.featureflag.RPKBukkitFeatureFlagCreateEvent
import com.rpkit.featureflags.bukkit.event.featureflag.RPKBukkitFeatureFlagDeleteEvent
import com.rpkit.featureflags.bukkit.event.featureflag.RPKBukkitFeatureFlagSetEvent
import com.rpkit.players.bukkit.profile.RPKProfile
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKFeatureFlagServiceImpl(override val plugin: RPKFeatureFlagsBukkit) : RPKFeatureFlagService {

    private val featureFlags = mutableMapOf<String, RPKFeatureFlag>()

    override fun getFeatureFlag(name: RPKFeatureFlagName): RPKFeatureFlag? {
        return featureFlags[name.value]
    }

    override fun createFeatureFlag(name: RPKFeatureFlagName, isEnabledByDefault: Boolean): RPKFeatureFlag? {
        val featureFlag = RPKFeatureFlagImpl(plugin, name, isEnabledByDefault)
        val event = RPKBukkitFeatureFlagCreateEvent(featureFlag, false)
        plugin.server.pluginManager.callEvent(event)
        return if (!event.isCancelled) {
            featureFlags[name.value] = featureFlag
            featureFlag
        } else {
            null
        }
    }

    override fun removeFeatureFlag(featureFlag: RPKFeatureFlag) {
        val event = RPKBukkitFeatureFlagDeleteEvent(featureFlag, false)
        plugin.server.pluginManager.callEvent(event)
        if (!event.isCancelled) {
            featureFlags.remove(featureFlag.name.value)
        }
    }

    override fun setFeatureFlag(profile: RPKProfile, featureFlag: RPKFeatureFlag, enabled: Boolean): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val event = RPKBukkitFeatureFlagSetEvent(profile, featureFlag, enabled, true)
            plugin.server.pluginManager.callEvent(event)
            if (event.isCancelled) return@runAsync
            val profileFeatureFlagTable = plugin.database.getTable(RPKProfileFeatureFlagTable::class.java)
            var profileFeatureFlag = profileFeatureFlagTable.get(event.profile, event.featureFlag).join()
            if (profileFeatureFlag == null) {
                profileFeatureFlag = RPKProfileFeatureFlag(
                    profile = event.profile,
                    featureFlag = event.featureFlag,
                    isEnabled = event.enabled
                )
                profileFeatureFlagTable.insert(profileFeatureFlag).join()
            } else {
                profileFeatureFlag.isEnabled = event.enabled
                profileFeatureFlagTable.update(profileFeatureFlag).join()
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to set feature flag", exception)
            throw exception
        }
    }

}