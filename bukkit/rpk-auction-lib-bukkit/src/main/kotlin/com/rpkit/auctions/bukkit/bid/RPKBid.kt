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

package com.rpkit.auctions.bukkit.bid

import com.rpkit.auctions.bukkit.auction.RPKAuction
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Entity

/**
 * Represents a bid.
 */
interface RPKBid: Entity {

    /**
     * The auction that this bid is for.
     */
    val auction: RPKAuction

    /**
     * The character that made this bid.
     */
    val character: RPKCharacter

    /**
     * The amount this bid is for. The currency is defined by the auction.
     */
    val amount: Int

}