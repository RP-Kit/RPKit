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

package com.rpkit.essentials.bukkit.kit

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.kit.bukkit.kit.RPKKit
import com.rpkit.kit.bukkit.kit.RPKKitName
import com.rpkit.kit.bukkit.kit.RPKKitService


class RPKKitServiceImpl(override val plugin: RPKEssentialsBukkit) : RPKKitService {

    override val kits: MutableList<RPKKit> = plugin.config.getList("kits") as MutableList<RPKKit>

    override fun getKit(name: RPKKitName): RPKKit? {
        return kits.firstOrNull { it.name.value == name.value }
    }

    override fun addKit(kit: RPKKit) {
        kits.add(kit)
        plugin.config.set("kits", kits)
        plugin.saveConfig()
    }

    override fun updateKit(kit: RPKKit) {
        removeKit(kit)
        addKit(kit)
    }

    override fun removeKit(kit: RPKKit) {
        kits.remove(getKit(kit.name))
        plugin.config.set("kits", kits)
        plugin.saveConfig()
    }
}