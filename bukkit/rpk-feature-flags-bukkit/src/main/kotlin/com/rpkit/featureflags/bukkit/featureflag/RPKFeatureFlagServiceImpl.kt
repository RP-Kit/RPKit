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

package com.rpkit.featureflags.bukkit.featureflag

import com.rpkit.featureflags.bukkit.RPKFeatureFlagsBukkit
import com.rpkit.featureflags.bukkit.database.table.RPKProfileFeatureFlagTable
import com.rpkit.featureflags.bukkit.event.featureflag.RPKBukkitFeatureFlagSetEvent
import com.rpkit.players.bukkit.profile.RPKProfile


class RPKFeatureFlagServiceImpl(override val plugin: RPKFeatureFlagsBukkit) : RPKFeatureFlagService {

    private val featureFlags = mutableMapOf<String, RPKFeatureFlag>()

    override fun getFeatureFlag(name: String): RPKFeatureFlag? {
        return featureFlags[name]
    }

    override fun createFeatureFlag(name: String, isEnabledByDefault: Boolean): RPKFeatureFlag {
        val featureFlag = RPKFeatureFlagImpl(plugin, name, isEnabledByDefault)
        featureFlags[name] = featureFlag
        return featureFlag
    }

    override fun removeFeatureFlag(featureFlag: RPKFeatureFlag) {
        featureFlags.remove(featureFlag.name)
    }

    override fun setFeatureFlag(profile: RPKProfile, featureFlag: RPKFeatureFlag, enabled: Boolean) {
        val event = RPKBukkitFeatureFlagSetEvent(profile, featureFlag, enabled)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val profileFeatureFlagTable = plugin.database.getTable(RPKProfileFeatureFlagTable::class.java)
        var profileFeatureFlag = profileFeatureFlagTable.get(event.profile, event.featureFlag)
        if (profileFeatureFlag == null) {
            profileFeatureFlag = RPKProfileFeatureFlag(
                    profile = event.profile,
                    featureFlag = event.featureFlag,
                    isEnabled = event.enabled
            )
            profileFeatureFlagTable.insert(profileFeatureFlag)
        } else {
            profileFeatureFlag.isEnabled = event.enabled
            profileFeatureFlagTable.update(profileFeatureFlag)
        }
    }

}