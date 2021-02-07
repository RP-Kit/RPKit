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

package com.rpkit.blocklog.bukkit.block

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.bukkit.inventory.ItemStack
import java.time.LocalDateTime


class RPKBlockInventoryChangeImpl(
        override var id: RPKBlockInventoryChangeId? = null,
        override val blockHistory: RPKBlockHistory,
        override val time: LocalDateTime,
        override val profile: RPKProfile?,
        override val minecraftProfile: RPKMinecraftProfile?,
        override val character: RPKCharacter?,
        override val from: Array<ItemStack>,
        override val to: Array<ItemStack>,
        override val reason: String
) : RPKBlockInventoryChange