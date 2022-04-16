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
import com.rpkit.core.command.result.IncorrectUsageFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.languages.bukkit.RPKLanguagesBukkit
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class LanguageCommand(private val plugin: RPKLanguagesBukkit) : RPKCommandExecutor {

    private val languageListCommand = LanguageListCommand(plugin)
    private val languageListUnderstandingCommand = LanguageListUnderstandingCommand(plugin)
    private val languageSetUnderstandingCommand = LanguageSetUnderstandingCommand(plugin)

    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        return if (args.isNotEmpty()) {
            val newArgs = args.drop(1).toTypedArray()
            when (args[0].lowercase()) {
                "list" -> languageListCommand.onCommand(sender, newArgs)
                "setunderstanding" -> languageSetUnderstandingCommand.onCommand(sender, newArgs)
                "listunderstanding" -> languageListUnderstandingCommand.onCommand(sender, newArgs)
                else -> {
                    sender.sendMessage(plugin.messages.languageUsage)
                    completedFuture(IncorrectUsageFailure())
                }
            }
        } else {
            sender.sendMessage(plugin.messages.languageUsage)
            completedFuture(IncorrectUsageFailure())
        }
    }
}
