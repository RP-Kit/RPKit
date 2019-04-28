/*
 * Copyright 2019 Ross Binden
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

package com.rpkit.itemquality.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.itemquality.bukkit.command.itemquality.ItemQualityCommand
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQualityProviderImpl
import com.rpkit.itemquality.bukkit.listener.PlayerItemDamageListener
import org.bstats.bukkit.Metrics


class RPKItemQualityBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        Metrics(this)
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKItemQualityProviderImpl(this)
        )
    }

    override fun registerListeners() {
        registerListeners(
                PlayerItemDamageListener(this)
        )
    }

    override fun registerCommands() {
        getCommand("itemquality")?.setExecutor(ItemQualityCommand(this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("not-from-console", "&cYou may not use this command from console.")
        messages.setDefault("no-permission-itemquality-list", "&cYou do not have permission to view a list of item qualities.")
        messages.setDefault("no-permission-itemquality-set", "&cYou do not have permission to set an item's quality.")
        messages.setDefault("itemquality-set-usage", "&cUsage: /itemquality set [quality]")
        messages.setDefault("itemquality-set-invalid-quality", "&cThere is no quality by that name.")
        messages.setDefault("itemquality-set-invalid-item-none", "&cYou must be holding an item to perform this command.")
        messages.setDefault("itemquality-set-valid", "&aItem quality set to \$quality")
        messages.setDefault("itemquality-list-title", "&7Item Qualities:")
        messages.setDefault("itemquality-list-item", "&7- &f\$quality")
        messages.setDefault("itemquality-usage", "&cUsage: /itemquality [list|set]")
    }

}