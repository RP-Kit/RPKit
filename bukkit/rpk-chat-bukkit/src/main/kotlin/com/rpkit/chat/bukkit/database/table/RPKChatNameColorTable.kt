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

package com.rpkit.chat.bukkit.database.table

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.database.create
import com.rpkit.chat.bukkit.database.jooq.Tables.RPKIT_CHAT_NAME_COLOR
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import java.util.concurrent.CompletableFuture
import com.rpkit.chat.bukkit.database.jooq.tables.records.RpkitChatNameColorRecord
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import java.util.concurrent.CompletableFuture.runAsync
import java.util.logging.Level.SEVERE

/**
 * Represents the chat name color table.
 */
class RPKChatNameColorTable(private val database: Database, private val plugin: RPKChatBukkit) : Table {

    fun insert(id: RPKMinecraftProfileId?, chatNameColor: String): CompletableFuture<Void> {
        // insert id -> chat name color record into database
        return runAsync {
            database.create
                .insertInto(
                    RPKIT_CHAT_NAME_COLOR,
                    RPKIT_CHAT_NAME_COLOR.MINECRAFT_PROFILE_ID,
                    RPKIT_CHAT_NAME_COLOR.CHAT_NAME_COLOR
                )
                .values(
                    id?.value,
                    chatNameColor
                )
                .execute()
        }.exceptionally { e ->
            plugin.logger.log(SEVERE, "Failed to insert chat name color record for Minecraft profile ${id?.value} and chat name color $chatNameColor", e)
            throw e
        }
    }

    fun update(minecraftProfileId: RPKMinecraftProfileId, chatNameColor: String): CompletableFuture<Void> {
        // update chat name color record in database
        return runAsync {
            database.create
                .update(RPKIT_CHAT_NAME_COLOR)
                .set(RPKIT_CHAT_NAME_COLOR.CHAT_NAME_COLOR, chatNameColor)
                .where(RPKIT_CHAT_NAME_COLOR.MINECRAFT_PROFILE_ID.eq(minecraftProfileId.value))
                .execute()
        }.exceptionally { e ->
            plugin.logger.log(SEVERE, "Failed to update chat name color record for Minecraft profile ${minecraftProfileId.value} and chat name color $chatNameColor", e)
            throw e
        }
    }

    operator fun get(id: RPKMinecraftProfileId): CompletableFuture<RpkitChatNameColorRecord?> {
        // get chat name color record from database by id
        return CompletableFuture.supplyAsync {
            database.create
                .selectFrom(RPKIT_CHAT_NAME_COLOR)
                .where(RPKIT_CHAT_NAME_COLOR.MINECRAFT_PROFILE_ID.eq(id.value))
                .fetchOne()
        }.exceptionally { e ->
            plugin.logger.log(SEVERE, "Failed to get chat name color record for Minecraft profile ${id.value}", e)
            null
        }
    }

    fun delete(): CompletableFuture<Void> {
        // delete chat name color record from database
        return runAsync {
            database.create
                .deleteFrom(RPKIT_CHAT_NAME_COLOR)
                .execute()
        }.exceptionally { e ->
            plugin.logger.log(SEVERE, "Failed to delete chat name color records", e)
            throw e
        }
    }
}