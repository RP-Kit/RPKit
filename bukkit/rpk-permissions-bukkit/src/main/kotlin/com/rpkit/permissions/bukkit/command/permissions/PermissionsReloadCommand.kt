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

package com.rpkit.permissions.bukkit.command.permissions

import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.CommandSuccess
import com.rpkit.core.command.result.MissingServiceFailure
import com.rpkit.core.command.result.NoPermissionFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroupService
import com.rpkit.permissions.bukkit.group.RPKGroupServiceImpl
import com.rpkit.permissions.bukkit.permissions.RPKPermissionsService
import com.rpkit.permissions.bukkit.permissions.RPKPermissionsServiceImpl
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class PermissionsReloadCommand(private val plugin: RPKPermissionsBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        if (!sender.hasPermission("rpkit.permissions.command.permissions.reload")) {
            sender.sendMessage(plugin.messages.noPermissionPermissionsReload)
            return completedFuture(NoPermissionFailure("rpkit.permissions.command.permissions.reload"))
        }
        val permissionsService = Services[RPKPermissionsService::class.java] as? RPKPermissionsServiceImpl
        if (permissionsService == null) {
            sender.sendMessage(plugin.messages.noPermissionsService)
            return completedFuture(MissingServiceFailure(RPKPermissionsService::class.java))
        }
        val groupService = Services[RPKGroupService::class.java] as? RPKGroupServiceImpl
        if (groupService == null) {
            sender.sendMessage(plugin.messages.noGroupService)
            return completedFuture(MissingServiceFailure(RPKGroupService::class.java))
        }
        plugin.reloadConfig()
        return groupService.reload().thenApply {
            plugin.server.scheduler.runTask(plugin, Runnable {
                permissionsService.reload().thenRun {
                    sender.sendMessage(plugin.messages.permissionsReloadValid)
                }
            })
            CommandSuccess
        }
    }
}