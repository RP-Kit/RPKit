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

/**
 * Represents a shop count.
 *
 * @property character The character
 * @property count The amount of shops owned by the character
 */
class RPKShopCount(
        val character: RPKCharacter,
        var count: Int
)