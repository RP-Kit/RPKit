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

package com.rpkit.core.bukkit.command.sender.resolver

import com.rpkit.core.bukkit.RPKCoreBukkit
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Service
import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture

class RPKBukkitCommandSenderResolutionService(override val plugin: RPKCoreBukkit) : Service {

    private val resolvers = mutableListOf<RPKBukkitCommandSenderResolver>()

    init {
        addResolver(RPKBukkitConsoleCommandSenderResolver())
        addResolver(RPKBukkitBlockCommandSenderResolver())
        addResolver(RPKBukkitRemoteCommandSenderResolver())
    }

    fun addResolver(resolver: RPKBukkitCommandSenderResolver) {
        resolvers.add(resolver)
    }

    fun resolve(bukkitCommandSender: CommandSender): RPKCommandSender? {
        return resolvers
            .sortedBy(RPKBukkitCommandSenderResolver::priority)
            .mapNotNull { resolver -> resolver.resolve(bukkitCommandSender) }
            .firstOrNull()
    }

}