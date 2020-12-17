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

package com.rpkit.shops.bukkit.shopcount

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.Service

/**
 * Provides shop count related operations.
 */
interface RPKShopCountService : Service {

    /**
     * Gets the shop count of a character.
     *
     * @param character The character
     * @return The amount of shops owned by the character
     */
    fun getShopCount(character: RPKCharacter): Int

    /**
     * Sets the amount of shops owned by a character.
     *
     * @param character The character
     * @param amount The amount of shops to set
     */
    fun setShopCount(character: RPKCharacter, amount: Int)

}