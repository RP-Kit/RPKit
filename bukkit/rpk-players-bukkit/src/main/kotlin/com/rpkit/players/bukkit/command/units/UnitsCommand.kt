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

package com.rpkit.players.bukkit.command.units

import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.command.result.NoProfileSelfFailure
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.toBukkitPlayer
import com.rpkit.players.bukkit.unit.*
import com.rpkit.players.bukkit.unit.UnitType.Companion.HEIGHT
import com.rpkit.players.bukkit.unit.UnitType.Companion.LONG_DISTANCE
import com.rpkit.players.bukkit.unit.UnitType.Companion.SHORT_DISTANCE
import com.rpkit.players.bukkit.unit.UnitType.Companion.WEIGHT
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class UnitsCommand(private val plugin: RPKPlayersBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (!sender.hasPermission("rpkit.players.command.units")) {
            sender.sendMessage(plugin.messages.noPermissionUnits)
            return completedFuture(NoPermissionFailure("rpkit.players.command.units"))
        }
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return completedFuture(NotAPlayerFailure())
        }
        val profile = sender.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages.noProfileSelf)
            return completedFuture(NoProfileSelfFailure())
        }
        val profileId = profile.id
        if (profileId == null) {
            sender.sendMessage(plugin.messages.noProfileSelf)
            return completedFuture(NoProfileSelfFailure())
        }
        val unitService = Services[RPKUnitService::class.java]
        if (unitService == null) {
            sender.sendMessage(plugin.messages.noUnitService)
            return completedFuture(MissingServiceFailure(RPKUnitService::class.java))
        }
        if (args.isNotEmpty() && args.first().equals("set", ignoreCase = true)) {
            if (args.size < 3) {
                sender.sendMessage(plugin.messages.unitsSetUsage)
                return completedFuture(IncorrectUsageFailure())
            }
            val unitType = try {
                UnitType.valueOf(args.drop(1).dropLast(1).joinToString(" ").uppercase())
            } catch (exception: IllegalArgumentException) {
                sender.sendMessage(plugin.messages.unitsSetInvalidUnitType)
                return completedFuture(IncorrectUsageFailure())
            }
            val unit = try {
                when (unitType) {
                    HEIGHT -> HeightUnit.valueOf(args.last())
                    WEIGHT -> WeightUnit.valueOf(args.last())
                    LONG_DISTANCE -> LongDistanceUnit.valueOf(args.last())
                    SHORT_DISTANCE -> ShortDistanceUnit.valueOf(args.last())
                    else -> throw IllegalArgumentException("Invalid unit type")
                }
            } catch (exception: IllegalArgumentException) {
                sender.sendMessage(plugin.messages.unitsSetInvalidUnit)
                return completedFuture(IncorrectUsageFailure())
            }
            return unitService.setPreferredUnit(profileId, unitType, unit).thenApply {
                sender.sendMessage(plugin.messages.unitsSetValid.withParameters(
                    unitType = unitType,
                    unit = unit
                ))
                plugin.server.scheduler.runTask(plugin, Runnable {
                    sender.toBukkitPlayer()?.performCommand("units")
                })
                CommandSuccess
            }
        } else {
            UnitType.values().forEach { unitType ->
                sendUnitPreference(sender, unitService, profileId, unitType)
            }
            return completedFuture(CommandSuccess)
        }
    }

    private fun sendUnitPreference(
        sender: RPKMinecraftProfile,
        unitService: RPKUnitService,
        profileId: RPKProfileId,
        unitType: UnitType
    ) {
        unitService.getPreferredUnit(profileId, unitType).thenAccept { preference ->
            val units = when (unitType) {
                HEIGHT -> HeightUnit.values()
                WEIGHT -> WeightUnit.values()
                LONG_DISTANCE -> LongDistanceUnit.values()
                SHORT_DISTANCE -> ShortDistanceUnit.values()
                else -> throw IllegalArgumentException("Invalid unit type")
            }
            val messageParts = buildList {
                addAll(TextComponent.fromLegacyText(plugin.messages.unitsPreference.withParameters(unitType = unitType)))
                addAll(units.flatMap { unit ->
                    listOf(
                        if (unit == preference) {
                            TextComponent(plugin.messages.unitsSelectedPreference.withParameters(unit = unit))
                        } else {
                            TextComponent(plugin.messages.unitsUnselectedPreference.withParameters(unit = unit))
                                .apply {
                                    hoverEvent = HoverEvent(
                                        SHOW_TEXT, Text(
                                            TextComponent.fromLegacyText(
                                                plugin.messages.unitsUnselectedPreferenceHover
                                                    .withParameters(unitType, unit)
                                            )
                                        )
                                    )
                                    clickEvent = ClickEvent(RUN_COMMAND, "/units set $unitType $unit")
                                }
                        },
                        TextComponent(" ")
                    )
                })
            }
            sender.sendMessage(*messageParts.toTypedArray())
        }
    }
}