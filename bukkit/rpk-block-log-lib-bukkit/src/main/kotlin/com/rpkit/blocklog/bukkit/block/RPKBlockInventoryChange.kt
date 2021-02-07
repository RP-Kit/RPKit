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


interface RPKBlockInventoryChange {

    var id: RPKBlockInventoryChangeId?
    val blockHistory: RPKBlockHistory
    val time: LocalDateTime
    val profile: RPKProfile?
    val minecraftProfile: RPKMinecraftProfile?
    val character: RPKCharacter?
    val from: Array<ItemStack>
    val to: Array<ItemStack>
    val reason: String

}