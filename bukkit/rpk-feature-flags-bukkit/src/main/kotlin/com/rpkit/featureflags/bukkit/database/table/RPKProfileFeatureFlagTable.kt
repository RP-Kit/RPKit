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

package com.rpkit.featureflags.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.service.Services
import com.rpkit.featureflags.bukkit.RPKFeatureFlagsBukkit
import com.rpkit.featureflags.bukkit.database.jooq.Tables.RPKIT_PROFILE_FEATURE_FLAG
import com.rpkit.featureflags.bukkit.featureflag.RPKFeatureFlag
import com.rpkit.featureflags.bukkit.featureflag.RPKProfileFeatureFlag
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileService
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder


class RPKProfileFeatureFlagTable(private val database: Database, private val plugin: RPKFeatureFlagsBukkit) : Table {

    private val cache = database.cacheManager.createCache("rpk-feature-flags-bukkit.rpkit_profile_feature_flag.id",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(String::class.java, RPKProfileFeatureFlag::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers * 50L)))

    fun insert(entity: RPKProfileFeatureFlag) {
        database.create
                .insertInto(
                        RPKIT_PROFILE_FEATURE_FLAG,
                        RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID,
                        RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_NAME,
                        RPKIT_PROFILE_FEATURE_FLAG.ENABLED
                )
                .values(
                        entity.profile.id,
                        entity.featureFlag.name,
                        entity.isEnabled
                )
                .execute()
    }

    fun update(entity: RPKProfileFeatureFlag) {
        database.create
                .update(RPKIT_PROFILE_FEATURE_FLAG)
                .set(RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID, entity.profile.id)
                .set(RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_NAME, entity.featureFlag.name)
                .set(RPKIT_PROFILE_FEATURE_FLAG.ENABLED, entity.isEnabled)
                .where(RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID.eq(entity.profile.id))
                .execute()
    }

    fun get(profile: RPKProfile, featureFlag: RPKFeatureFlag): RPKProfileFeatureFlag? {
        if (cache.containsKey(featureFlag.name)) {
            return cache[featureFlag.name]
        } else {
            val result = database.create
                    .select(
                            RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID,
                            RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_NAME,
                            RPKIT_PROFILE_FEATURE_FLAG.ENABLED
                    )
                    .from(RPKIT_PROFILE_FEATURE_FLAG)
                    .where(RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID.eq(profile.id))
                    .and(RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_NAME.eq(featureFlag.name))
                    .fetchOne() ?: return null
            Services[RPKProfileService::class]
                    ?: return null
            val profileFeatureFlag = RPKProfileFeatureFlag(
                    profile,
                    featureFlag,
                    result.get(RPKIT_PROFILE_FEATURE_FLAG.ENABLED)
            )
            cache.put(featureFlag.name, profileFeatureFlag)
            return profileFeatureFlag
        }
    }

    fun delete(entity: RPKProfileFeatureFlag) {
        database.create
                .deleteFrom(RPKIT_PROFILE_FEATURE_FLAG)
                .where(RPKIT_PROFILE_FEATURE_FLAG.PROFILE_ID.eq(entity.profile.id))
                .and(RPKIT_PROFILE_FEATURE_FLAG.FEATURE_FLAG_NAME.eq(entity.featureFlag.name))
                .execute()
        cache.remove(entity.featureFlag.name)
    }

}