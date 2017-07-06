package com.rpkit.drinks.bukkit.listener

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.drink.bukkit.drink.RPKDrinkProvider
import com.rpkit.drinks.bukkit.RPKDrinksBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerItemConsumeEvent


class PlayerItemConsumeListener(private val plugin: RPKDrinksBukkit): Listener {

    @EventHandler
    fun onPlayerItemConsume(event: PlayerItemConsumeEvent) {
        val drinkProvider = plugin.core.serviceManager.getServiceProvider(RPKDrinkProvider::class)
        val drink = drinkProvider.getDrink(event.item) ?: return
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player) ?: return
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val character = characterProvider.getActiveCharacter(minecraftProfile) ?: return
        drinkProvider.setDrunkenness(character, drinkProvider.getDrunkenness(character) + drink.drunkenness)
    }

}