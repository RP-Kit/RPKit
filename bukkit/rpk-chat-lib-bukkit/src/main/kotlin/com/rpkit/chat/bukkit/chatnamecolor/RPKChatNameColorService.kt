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

 package com.rpkit.chat.bukkit.chatnamecolor

 import com.rpkit.core.service.Service
 import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
 
 /**
  * Provides operations related to overridden chat name colors
  */
 interface RPKChatNameColorService : Service {
    
    /**
     * Gets the chat name color for a player.
     * If the player has no chat name color, null is returned.
     * 
     * @param minecraftProfile The player to get the chat name color of
     * @return The chat name color, or null if the player has no chat name color
     */
     fun getChatNameColor(minecraftProfile: RPKMinecraftProfile?): String?
 }
 