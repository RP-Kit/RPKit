/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.characters.bukkit.character

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.field.HideableCharacterCardField
import com.rpkit.characters.bukkit.character.field.RPKCharacterCardFieldService
import com.rpkit.characters.bukkit.race.RPKRace
import com.rpkit.characters.bukkit.species.RPKSpecies
import com.rpkit.characters.bukkit.species.RPKSpeciesName
import com.rpkit.characters.bukkit.species.RPKSpeciesService
import com.rpkit.core.bukkit.location.toRPKLocation
import com.rpkit.core.location.RPKLocation
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.inventory.ItemStack
import java.util.concurrent.CompletableFuture
import java.util.logging.Level

/**
 * Character implementation.
 */
class RPKCharacterImpl(
    val plugin: RPKCharactersBukkit,
    override var id: RPKCharacterId? = null,
    override var profile: RPKProfile? = null,
    override var minecraftProfile: RPKMinecraftProfile? = null,
    name: String = plugin.config.getString("characters.defaults.name") ?: "",
    override var gender: String? = plugin.config.getString("characters.defaults.gender"),
    override var age: Int = plugin.config.getInt("characters.defaults.age"),
    species: RPKSpecies? = plugin.config.getString("characters.defaults.species")
            ?.let { Services[RPKSpeciesService::class.java]?.getSpecies(RPKSpeciesName(it)) },
    description: String = plugin.config.getString("characters.defaults.description") ?: "",
    override var weight: Double? = plugin.config.getDouble("characters.defaults.weight") ?: null,
    dead: Boolean = plugin.config.getBoolean("characters.defaults.dead"),
    override var location: RPKLocation = Bukkit.getWorlds()[0].spawnLocation.toRPKLocation(),
    override var inventoryContents: Array<ItemStack?> = (plugin.config.getList("characters.defaults.inventory-contents") as MutableList<ItemStack?>).toTypedArray(),
    override var helmet: ItemStack? = plugin.config.getItemStack("characters.defaults.helmet"),
    override var chestplate: ItemStack? = plugin.config.getItemStack("characters.defaults.chestplate"),
    override var leggings: ItemStack? = plugin.config.getItemStack("characters.defaults.leggings"),
    override var boots: ItemStack? = plugin.config.getItemStack("characters.defaults.boots"),
    override var health: Double = plugin.config.getDouble("characters.defaults.health"),
    override var maxHealth: Double = plugin.config.getDouble("characters.defaults.max-health"),
    override var mana: Int = plugin.config.getInt("characters.defaults.mana"),
    override var maxMana: Int = plugin.config.getInt("characters.defaults.max-mana"),
    override var foodLevel: Int = plugin.config.getInt("characters.defaults.food-level"),
    override var thirstLevel: Int = plugin.config.getInt("characters.defaults.thirst-level"),
    override var isProfileHidden: Boolean = plugin.config.getBoolean("characters.defaults.profile-hidden"),
    override var isNameHidden: Boolean = plugin.config.getBoolean("characters.defaults.name-hidden"),
    override var isGenderHidden: Boolean = plugin.config.getBoolean("characters.defaults.gender-hidden"),
    override var isAgeHidden: Boolean = plugin.config.getBoolean("characters.defaults.age-hidden"),
    override var isSpeciesHidden: Boolean = plugin.config.getBoolean("characters.defaults.species-hidden"),
    override var isDescriptionHidden: Boolean = plugin.config.getBoolean("characters.defaults.description-hidden"),
    override var isWeightHidden: Boolean = plugin.config.getBoolean("characters.defaults.weight-hidden")
) : RPKCharacter {

    override var name = name
        set(name) {
            field = name
            if (plugin.config.getBoolean("characters.set-player-display-name")) {
                val minecraftProfile = minecraftProfile
                if (minecraftProfile != null) {
                    plugin.server.getPlayer(minecraftProfile.minecraftUUID)?.setDisplayName(name)
                }
            }
        }

    @Deprecated("Use species", ReplaceWith("species"))
    override var race: RPKRace?
        get() = species
        set(value) {
            species = value
        }

    override var species = species
        set(species) {
            age = age.coerceIn(
                species?.minAge ?: plugin.config.getInt("characters.min-age"),
                species?.maxAge ?: plugin.config.getInt("characters.max-age")
            )
            field = species
        }

    override var description = description
        set(description) {
            field = description
            if (field.length > 1024) {
                field = field.substring(0, 1021) + "..."
            }
        }

    override var isDead = dead

    @Deprecated("Use isSpeciesHidden", replaceWith = ReplaceWith("isSpeciesHidden"))
    override var isRaceHidden: Boolean
        get() = isSpeciesHidden
        set(value) {
            isSpeciesHidden = value
        }

    override fun showCharacterCard(minecraftProfile: RPKMinecraftProfile) {
        CompletableFuture.runAsync {
            val characterCardFieldService = Services[RPKCharacterCardFieldService::class.java]
            if (characterCardFieldService == null) {
                minecraftProfile.sendMessage(plugin.messages["no-character-card-field-service"])
                return@runAsync
            }
            val profile = minecraftProfile.profile
            for (line in if ((profile as? RPKProfile)?.id?.value == this.profile?.id?.value) plugin.messages.getList("character-card-owner") else
                plugin.messages.getList("character-card-not-owner")) {
                val messageComponents = mutableListOf<BaseComponent>()
                var chatColor: ChatColor? = null
                var chatFormat: ChatColor? = null
                var i = 0
                while (i < line.length) {
                    if (line[i] == ChatColor.COLOR_CHAR) {
                        val colourOrFormat = ChatColor.getByChar(line[i + 1])
                        if (colourOrFormat?.isColor == true) {
                            chatColor = colourOrFormat
                            chatFormat = null
                        }
                        if (colourOrFormat?.isFormat == true) chatFormat = colourOrFormat
                        i += 1
                    } else {
                        var fieldFound = false
                        characterCardFieldService.characterCardFields
                            .filter { field -> line.length >= i + "\${${field.name}}".length }
                            .filter { field ->
                                line.substring(
                                    i,
                                    i + "\${${field.name}}".length
                                ) == "\${${field.name}}"
                            }
                            .forEach { field ->
                                val textComponent = TextComponent(
                                    if (profile is RPKProfile)
                                        field.get(this, profile).join()
                                    else
                                        field.get(this).join()
                                )
                                if (chatColor != null) {
                                    textComponent.color = chatColor.asBungee()
                                }
                                if (chatFormat != null) {
                                    textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                    textComponent.isBold = chatFormat == ChatColor.BOLD
                                    textComponent.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                                    textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                    textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                }
                                messageComponents.add(textComponent)
                                i += "\${${field.name}}".length - 1
                                fieldFound = true
                            }
                        if (!fieldFound) {
                            var editFound = false
                            characterCardFieldService.characterCardFields
                                .filter { field -> line.length >= i + "\${edit(${field.name})}".length }
                                .filter { field ->
                                    line.substring(
                                        i,
                                        i + "\${edit(${field.name})}".length
                                    ) == "\${edit(${field.name})}"
                                }
                                .forEach { field ->
                                    if (minecraftProfile.id?.value == this.minecraftProfile?.id?.value) {
                                        val editComponent = TextComponent("Edit")
                                        editComponent.clickEvent =
                                            ClickEvent(ClickEvent.Action.RUN_COMMAND, "/character set ${field.name}")
                                        editComponent.hoverEvent = HoverEvent(
                                            HoverEvent.Action.SHOW_TEXT,
                                            Text("Click to change your character's ${field.name}")
                                        )
                                        if (chatColor != null) {
                                            editComponent.color = chatColor.asBungee()
                                        }
                                        if (chatFormat != null) {
                                            editComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                            editComponent.isBold = chatFormat == ChatColor.BOLD
                                            editComponent.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                                            editComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                            editComponent.isItalic = chatFormat == ChatColor.ITALIC
                                        }
                                        messageComponents.add(editComponent)
                                    }
                                    i += "\${edit(${field.name})}".length - 1
                                    editFound = true
                                }
                            if (!editFound) {
                                var hideFound = false
                                characterCardFieldService.characterCardFields
                                    .filter { field -> line.length >= i + "\${hide(${field.name})}".length }
                                    .filter { field ->
                                        line.substring(
                                            i,
                                            i + "\${hide(${field.name})}".length
                                        ) == "\${hide(${field.name})}"
                                    }
                                    .filterIsInstance<HideableCharacterCardField>()
                                    .forEach { field ->
                                        if (minecraftProfile.id?.value == this.minecraftProfile?.id?.value) {
                                            if (field.isHidden(this).join()) {
                                                val unhideComponent = TextComponent("Unhide")
                                                unhideComponent.clickEvent = ClickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/character unhide ${field.name}"
                                                )
                                                unhideComponent.hoverEvent = HoverEvent(
                                                    HoverEvent.Action.SHOW_TEXT,
                                                    Text("Click to unhide your character's ${field.name}")
                                                )
                                                messageComponents.add(unhideComponent)
                                            } else {
                                                val hideComponent = TextComponent("Hide")
                                                hideComponent.clickEvent = ClickEvent(
                                                    ClickEvent.Action.RUN_COMMAND,
                                                    "/character hide ${field.name}"
                                                )
                                                hideComponent.hoverEvent = HoverEvent(
                                                    HoverEvent.Action.SHOW_TEXT,
                                                    Text("Click to hide your character's ${field.name}")
                                                )
                                                messageComponents.add(hideComponent)
                                            }
                                            if (chatColor != null) {
                                                messageComponents.last().color = chatColor.asBungee()
                                            }
                                            if (chatFormat != null) {
                                                messageComponents.last().isObfuscated = chatFormat == ChatColor.MAGIC
                                                messageComponents.last().isBold = chatFormat == ChatColor.BOLD
                                                messageComponents.last().isStrikethrough =
                                                    chatFormat == ChatColor.STRIKETHROUGH
                                                messageComponents.last().isUnderlined =
                                                    chatFormat == ChatColor.UNDERLINE
                                                messageComponents.last().isItalic = chatFormat == ChatColor.ITALIC
                                            }
                                        }
                                        i += "\${hide(${field.name})}".length - 1
                                        hideFound = true
                                    }
                                if (!hideFound) {
                                    val textComponent = TextComponent(line[i].toString())
                                    if (chatColor != null) {
                                        textComponent.color = chatColor.asBungee()
                                    }
                                    if (chatFormat != null) {
                                        textComponent.isObfuscated = chatFormat == ChatColor.MAGIC
                                        textComponent.isBold = chatFormat == ChatColor.BOLD
                                        textComponent.isStrikethrough = chatFormat == ChatColor.STRIKETHROUGH
                                        textComponent.isUnderlined = chatFormat == ChatColor.UNDERLINE
                                        textComponent.isItalic = chatFormat == ChatColor.ITALIC
                                    }
                                    messageComponents.add(textComponent)
                                }
                            }
                        }
                    }
                    i++
                }
                minecraftProfile.sendMessage(*messageComponents.toTypedArray())
            }
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to show character card", exception)
            throw exception
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as RPKCharacterImpl

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.value ?: 0
    }


}
