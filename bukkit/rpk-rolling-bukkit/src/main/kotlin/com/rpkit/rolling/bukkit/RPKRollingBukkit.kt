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

package com.rpkit.rolling.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bstats.bukkit.Metrics


class RPKRollingBukkit : RPKBukkitPlugin() {

    override fun onEnable() {
        System.setProperty("com.rpkit.rolling.bukkit.shadow.impl.org.jooq.no-logo", "true")

        Metrics(this, 4410)
        saveDefaultConfig()
    }

    override fun registerCommands() {
        getCommand("roll")?.setExecutor(RollCommand(this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("roll", "&f\$character/\$player rolled &7\$roll &ffrom &7\$dice")
        messages.setDefault("roll-invalid-parse", "&cFailed to parse your roll.")
        messages.setDefault("roll-usage", "&cUsage: /roll [roll]")
        messages.setDefault("not-from-console", "&cYou must be a player to perform that command.")
        messages.setDefault("no-character", "&cYou must have a character to perform that command.")
        messages.setDefault("no-minecraft-profile-service", "&cThere is no Minecraft profile service available.")
        messages.setDefault("no-character-service", "&cThere is no character service available.")
    }

}
