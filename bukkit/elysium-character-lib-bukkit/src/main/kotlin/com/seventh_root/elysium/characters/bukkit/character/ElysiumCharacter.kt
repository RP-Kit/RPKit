/*
 * Copyright 2016 Ross Binden
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

package com.seventh_root.elysium.characters.bukkit.character

import com.seventh_root.elysium.characters.bukkit.gender.ElysiumGender
import com.seventh_root.elysium.characters.bukkit.gender.ElysiumGenderProvider
import com.seventh_root.elysium.characters.bukkit.race.ElysiumRace
import com.seventh_root.elysium.characters.bukkit.race.ElysiumRaceProvider
import com.seventh_root.elysium.core.database.Entity
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer
import org.bukkit.Location
import org.bukkit.inventory.ItemStack

/**
 * Represents a character.
 * Players may have multiple characters, so data that is dependant on the character is stored in the character.
 * Each player has a currently-active character retrievable from the [ElysiumCharacterProvider].
 */
interface ElysiumCharacter: Entity {

    /**
     * The player.
     * Only this player may use the character.
     * Some characters may not have a player assigned, in which case this will be null.
     * It is important to remember the player may not always be playing the character, so operations like inventory
     * modification should be checked before directly modifying the player's inventory on platforms such as Bukkit.
     */
    var player: ElysiumPlayer?

    /**
     * The name of the character.
     * This includes all titles, surnames, and so forth. There are no separate fields for other parts of the name.
     * This may never be null.
     */
    var name: String

    /**
     * The gender of the character.
     * May be set to any gender implementation, as long as it has been registered with the [ElysiumGenderProvider].
     * This may be set to null, which is the current default in the config, as genders must be set up by the admins of
     * the server.
     */
    var gender: ElysiumGender?

    /**
     * The age of the character.
     * The character implementation may define acceptable bounds for this.
     */
    var age: Int

    /**
     * The race of the character.
     * May be set to any race implementation, as long as it has been registered with the [ElysiumRaceProvider].
     * This may be set to null, which is the current default in the config, as races must be set up by the admins of
     * the server.
     */
    var race: ElysiumRace?

    /**
     * The description of the character.
     * This is a human-readable description of what the character looks like, and what other characters would experience
     * upon meeting them for the first time.
     * Its length may be limited by implementations.
     */
    var description: String

    /**
     * Whether the character is dead or not.
     * If a character is currently dead, they should not be playable.
     */
    var isDead: Boolean

    /**
     * The location of the character.
     * If the character is currently being played, this is not necessarily up-to-date, and may represent the location of
     * the character last time they were switched to. In this situation you may wish to use the location of the player
     * currently playing the character instead.
     */
    var location: Location

    /**
     * The inventory contents of the character.
     * If the character is currently being played, this is not necessarily up-to-date, and may represent the inventory
     * contents of the character last time they were switched to. In this situation you may wish to use the inventory
     * contents of the player currently playing the character instead.
     */
    var inventoryContents: Array<ItemStack>

    /**
     * The helmet of the character.
     * If the character is currently being played, this is not necessarily up-to-date, and may represent the helmet of
     * the character last time they were switched to. In this situation you may wish to use the helmet of the player
     * currently playing the character instead.
     * The helmet may be null if the character is not wearing a helmet.
     */
    var helmet: ItemStack?

    /**
     * The chestplate of the character.
     * If the character is currently being played, this is not necessarily up-to-date, and may represent the chestplate
     * of the character last time they were switched to. In this situation you may wish to use the chestplate of the
     * player currently playing the character instead.
     * The chestplate may be null if the character is not wearing a chestplate.
     */
    var chestplate: ItemStack?

    /**
     * The leggings of the character.
     * If the character is currently being played, this is not necessarily up-to-date, and may represent the leggings
     * of the character last time they were switched to. In this situation you may wish to use the leggings of the
     * player currently playing the character instead.
     * The leggings may be null if the character is not wearing leggings.
     */
    var leggings: ItemStack?

    /**
     * The boots of the character.
     * If the character is currently being played, this is not necessarily up-to-date, and may represent the boots of
     * the character last time they were switched to. In this situation you may wish to use the boots of the player
     * currently playing the character instead.
     * The boots may be null if the character is not wearing boots.
     */
    var boots: ItemStack?

    /**
     * The health of the character.
     * If the character is currently being played, this is not necessarily up-to-date, and may represent the health of
     * the character last time they were switched to. In this situation you may wish to use the health of the player
     * currently playing the character instead.
     * The health is never null.
     */
    var health: Double

    /**
     * The maximum health of the character.
     * If the character is currently being played, this is not necessarily up-to-date, and may represent the max health
     * of the character last time they were switched to. In this situation you may wish to use the maximum health of the
     * player currently playing the character instead.
     * The maximum health is never null, and should never be set to values less or equal to zero, as this may cause
     * errors on the server.
     */
    var maxHealth: Double

    /**
     * The mana of the character.
     * This represents the current mana, as opposed to the size of the entire mana pool before spells are used. If
     * this is what you want, use [maxMana] instead.
     * Mana is used to cast spells if a spells plugin is used. It is provided here for convenience due to its heavy
     * usage by RPG-style systems.
     * Some servers may completely ignore mana if they remove it from the character card in the config.
     */
    var mana: Int

    /**
     * The maximum mana of the character.
     * This represents the size of the entire mana pool. [mana] should not be set higher than this.
     * Mana is used to cast spells if a spells plugin is used. It is provided here for convenience due to its heavy
     * usage by RPG-style systems.
     * Some servers may completely ignore mana if they remove it from the character card in the config.
     * Characters implementations may or may not have built-in mana regeneration. Spells plugins therefore should make
     * regeneration configurable in order to allow servers to customise this how they wish.
     */
    var maxMana: Int

    /**
     * The food level of the character.
     * If the character is currently being played, this is not necessarily up-to-date, and may represent the food level
     * of the character last time they were switched to. In this situation you may wish to use the food level of the
     * player currently playing the character instead.
     * The food level is never null, and the maximum is determined by Minecraft, as of 1.10 this is 20.
     */
    var foodLevel: Int

    /**
     * The thirst level of the character.
     * Thirst is sometimes provided by characters implementations to supplement Minecraft's hunger system. Some servers
     * see this as adding more realism, whereas others may deem it an annoyance, and others will leave it up to players
     * whether they wish to have thirst enabled or not.
     * The maximum thirst is always 20 to mimic the hunger system.
     */
    var thirstLevel: Int

    /**
     * Whether the player playing the character is hidden on the character's character card. If the player is hidden,
     * it is a good idea to maintain this behaviour in other plugins as well (at least to other players).
     * As of Minecraft 1.10, it is impossible to hide the nameplate while keeping the skin of the player. (This was
     * possible in Minecraft 1.7 for a short time)
     */
    var isPlayerHidden: Boolean

    /**
     * Whether the name of the character is hidden on the character's character card.
     * This may be done when a player wishes to conceal their character's identity, and allows easy hiding/unhiding
     * without having to rewrite the character's name.
     */
    var isNameHidden: Boolean

    /**
     * Whether the character's gender is hidden on the character's character card.
     * This prevents other players from immediately knowing the character's gender, e.g. if the player wishes to
     * present a character as androgynous.
     */
    var isGenderHidden: Boolean

    /**
     * Whether the character's age is hidden on the character's character card.
     * An example use of this is if a character is supernatural and may have lived for thousands of years, and a player
     * does not wish to reveal this.
     */
    var isAgeHidden: Boolean

    /**
     * Whether the character's race is hidden on the character's character card.
     * If a character is, for example, a half-elf and wishes to fit into a human society this may be of use.
     */
    var isRaceHidden: Boolean

    /**
     * Whether the description of the character is hidden.
     * An example use case is an invisible character, which is revealed at some point and only then displays their
     * description.
     */
    var isDescriptionHidden: Boolean

    /**
     * Shows the character card to the given player.
     * How this is done may be implementation-dependant, but the most common usage will be to show this in Minecraft.
     */
    fun showCharacterCard(player: ElysiumPlayer)

}