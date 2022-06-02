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
import com.rpkit.players.bukkit.profile.RPKProfile
import java.util.concurrent.CompletableFuture
import java.util.logging.Level


class RPKFeatureFlagImpl(
    private val plugin: RPKFeatureFlagsBukkit,
    override val name: RPKFeatureFlagName,
    override var isEnabledByDefault: Boolean
) : RPKFeatureFlag {

    override fun isEnabledFor(profile: RPKProfile): CompletableFuture<Boolean> {
        return plugin.database.getTable(RPKProfileFeatureFlagTable::class.java).get(profile, this).thenApply {
            it?.isEnabled ?: isEnabledByDefault
        }
    }

    override fun setEnabledFor(profile: RPKProfile, enabled: Boolean): CompletableFuture<Void> {
        return CompletableFuture.runAsync {
            val profileFeatureFlagTable = plugin.database.getTable(RPKProfileFeatureFlagTable::class.java)
            if (isEnabledFor(profile).join() != enabled) {
                var playerFeatureFlag = profileFeatureFlagTable.get(profile, this).join()
                if (playerFeatureFlag == null) {
                    playerFeatureFlag =
                        RPKProfileFeatureFlag(profile = profile, featureFlag = this, isEnabled = enabled)
                    profileFeatureFlagTable.insert(playerFeatureFlag).join()
                } else {
                    playerFeatureFlag.isEnabled = enabled
                    profileFeatureFlagTable.update(playerFeatureFlag).join()
                }
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to set feature flag enabled state", exception)
            throw exception
        }
    }

}