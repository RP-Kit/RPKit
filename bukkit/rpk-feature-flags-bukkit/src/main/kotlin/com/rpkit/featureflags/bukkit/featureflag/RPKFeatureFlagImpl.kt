/*
 * Copyright 2021 Ren Binden
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
import com.rpkit.players.bukkit.profile.RPKProfile


class RPKFeatureFlagImpl(
    private val plugin: RPKFeatureFlagsBukkit,
    override val name: RPKFeatureFlagName,
    override var isEnabledByDefault: Boolean
) : RPKFeatureFlag {

    override fun isEnabledFor(profile: RPKProfile): Boolean {
        return plugin.database.getTable(RPKProfileFeatureFlagTable::class.java).get(profile, this)?.isEnabled
                ?: isEnabledByDefault
    }

    override fun setEnabledFor(profile: RPKProfile, enabled: Boolean) {
        val profileFeatureFlagTable = plugin.database.getTable(RPKProfileFeatureFlagTable::class.java)
        if (isEnabledFor(profile) != enabled) {
            var playerFeatureFlag = profileFeatureFlagTable.get(profile, this)
            if (playerFeatureFlag == null) {
                playerFeatureFlag = RPKProfileFeatureFlag(profile = profile, featureFlag = this, isEnabled = enabled)
                profileFeatureFlagTable.insert(playerFeatureFlag)
            } else {
                playerFeatureFlag.isEnabled = enabled
                profileFeatureFlagTable.update(playerFeatureFlag)
            }
        }
    }

}