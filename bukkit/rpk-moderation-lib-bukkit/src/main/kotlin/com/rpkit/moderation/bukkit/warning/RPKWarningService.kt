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

package com.rpkit.moderation.bukkit.warning

import com.rpkit.core.service.Service
import com.rpkit.players.bukkit.profile.RPKProfile
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture

/**
 * Provides warning-related functionality
 */
interface RPKWarningService : Service {

    /**
     * Gets a warning by ID.
     *
     * @param id The ID
     * @return The warning
     */
    fun getWarning(id: RPKWarningId): CompletableFuture<out RPKWarning?>

    /**
     * Gets warnings issued to a player.
     *
     * @param profile The profile of the player
     * @return A list of warnings issued to the player
     */
    fun getWarnings(profile: RPKProfile): CompletableFuture<List<RPKWarning>>

    /**
     * Adds a warning.
     *
     * @param warning The warning to add
     */
    fun addWarning(warning: RPKWarning): CompletableFuture<Void>

    fun createWarning(
        reason: String,
        profile: RPKProfile,
        issuer: RPKProfile,
        time: LocalDateTime
    ): CompletableFuture<RPKWarning>

    /**
     * Removes a warning.
     *
     * @param warning The warning to remove
     */
    fun removeWarning(warning: RPKWarning): CompletableFuture<Void>

    /**
     * Updates a warning
     *
     * @param warning the warning to update
     */
    fun updateWarning(warning: RPKWarning): CompletableFuture<Void>

}