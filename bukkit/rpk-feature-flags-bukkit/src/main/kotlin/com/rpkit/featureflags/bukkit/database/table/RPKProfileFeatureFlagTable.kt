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

package com.rpkit.featureflags.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.featureflags.bukkit.RPKFeatureFlagsBukkit
import com.rpkit.featureflags.bukkit.database.create
import com.rpkit.featureflags.bukkit.database.jooq.Tables.RPKIT_PROFILE_FEATURE_FLAG
import com.rpkit.featureflags.bukkit.featureflag.RPKFeatureFlag
import com.rpkit.featureflags.bukkit.featureflag.RPKProfileFeatureFlag
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileService


class RPKProfileFeatureFlagTable(private val database: Database, private val plugin: RPKFeatureFlagsBukkit) : Table {

    private val cache = if (plugin.config.getBoolean("caching.rpkit_profile_feature_flag.id.enabled")) {
        database.cacheManager.createCache(
            "rpk-feature-flags-bukkit.rpkit_profile_feature_flag.id",
            String::class.java,
            RPKProfileFeatureFlag::class.java,
            plugin.config.getLong("caching.rpkit_profile_feature_flag.id.size")
        )
    } else null

    fun insert(entity: RPKProfileFeatureFlag) {
        val profileId = entity.profile.id ?: return
        database.create
                .insertInto(
                        RPKIT_PROFILE_FEATURE_FLAG,
                        RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID,
                        RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_NAME,
                        RPKIT_PROFILE_FEATURE_FLAG.ENABLED
                )
                .values(
                        profileId.value,
                        entity.featureFlag.name.value,
                        entity.isEnabled
                )
                .execute()
    }

    fun update(entity: RPKProfileFeatureFlag) {
        val profileId = entity.profile.id ?: return
        database.create
                .update(RPKIT_PROFILE_FEATURE_FLAG)
                .set(RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID, profileId.value)
                .set(RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_NAME, entity.featureFlag.name.value)
                .set(RPKIT_PROFILE_FEATURE_FLAG.ENABLED, entity.isEnabled)
                .where(RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID.eq(profileId.value))
                .execute()
    }

    fun get(profile: RPKProfile, featureFlag: RPKFeatureFlag): RPKProfileFeatureFlag? {
        if (cache?.containsKey(featureFlag.name.value) == true) {
            return cache[featureFlag.name.value]
        }
        val profileId = profile.id ?: return null
        val result = database.create
            .select(
                RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID,
                RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_NAME,
                RPKIT_PROFILE_FEATURE_FLAG.ENABLED
            )
            .from(RPKIT_PROFILE_FEATURE_FLAG)
            .where(RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID.eq(profileId.value))
            .and(RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_NAME.eq(featureFlag.name.value))
            .fetchOne() ?: return null
        Services[RPKProfileService::class.java]
            ?: return null
        val profileFeatureFlag = RPKProfileFeatureFlag(
            profile,
            featureFlag,
            result.get(RPKIT_PROFILE_FEATURE_FLAG.ENABLED)
        )
        cache?.set(featureFlag.name.value, profileFeatureFlag)
        return profileFeatureFlag
    }

    fun delete(entity: RPKProfileFeatureFlag) {
        val profileId = entity.profile.id ?: return
        database.create
                .deleteFrom(RPKIT_PROFILE_FEATURE_FLAG)
                .where(RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID.eq(profileId.value))
                .and(RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_NAME.eq(entity.featureFlag.name.value))
                .execute()
        cache?.remove(entity.featureFlag.name.value)
    }

}