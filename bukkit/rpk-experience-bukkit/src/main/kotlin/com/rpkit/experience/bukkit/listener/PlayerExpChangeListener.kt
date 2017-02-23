package com.rpkit.experience.bukkit.listener

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerExpChangeEvent


class PlayerExpChangeListener: Listener {

    @EventHandler
    fun onPlayerExpChange(event: PlayerExpChangeEvent) {
        event.amount = 0
    }

}