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

package com.rpkit.rolling.bukkit

import com.rpkit.core.bukkit.command.toBukkit
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.core.service.Services
import com.rpkit.rolling.bukkit.command.PrivateRollCommand
import com.rpkit.rolling.bukkit.command.RollCommand
import com.rpkit.rolling.bukkit.command.turnorder.TurnOrderCommand
import com.rpkit.rolling.bukkit.messages.RollingMessages
import com.rpkit.rolling.bukkit.turnorder.RPKTurnOrderService
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin


class RPKRollingBukkit : JavaPlugin(), RPKPlugin {

    lateinit var messages: RollingMessages

    override fun onEnable() {
        System.setProperty("com.rpkit.rolling.bukkit.shadow.impl.org.jooq.no-logo", "true")
        System.setProperty("com.rpkit.rolling.bukkit.shadow.impl.org.jooq.no-tips", "true")

        Metrics(this, 4410)
        saveDefaultConfig()

        messages = RollingMessages(this)

        Services[RPKTurnOrderService::class.java] = RPKTurnOrderService(this)

        registerCommands()
    }

    fun registerCommands() {
        getCommand("roll")?.setExecutor(RollCommand(this))
        getCommand("privateroll")?.setExecutor(PrivateRollCommand(this))
        getCommand("turnorder")?.setExecutor(TurnOrderCommand(this).toBukkit())
    }

}
