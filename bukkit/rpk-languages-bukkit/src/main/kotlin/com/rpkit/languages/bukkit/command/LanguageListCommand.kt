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

package com.rpkit.languages.bukkit.command

import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.CommandSuccess
import com.rpkit.core.command.result.MissingServiceFailure
import com.rpkit.core.command.result.NoPermissionFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.languages.bukkit.RPKLanguagesBukkit
import com.rpkit.languages.bukkit.language.RPKLanguageService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class LanguageListCommand(private val plugin: RPKLanguagesBukkit) : RPKCommandExecutor {

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        if (!sender.hasPermission("rpkit.languages.command.language.list")) {
            sender.sendMessage(plugin.messages.noPermissionLanguageList)
            return completedFuture(NoPermissionFailure("rpkit.languages.command.language.list"))
        }
        val languageService = Services[RPKLanguageService::class.java]
        if (languageService == null) {
            sender.sendMessage(plugin.messages.noLanguageService)
            return completedFuture(MissingServiceFailure(RPKLanguageService::class.java))
        }
        sender.sendMessage(plugin.messages.languageListTitle)
        for (language in languageService.languages) {
            sender.sendMessage(plugin.messages.languageListItem.withParameters(
                language = language
            ))
        }
        return completedFuture(CommandSuccess)
    }
}
