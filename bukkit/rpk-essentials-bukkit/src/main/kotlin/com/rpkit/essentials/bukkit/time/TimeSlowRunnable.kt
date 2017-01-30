package com.rpkit.essentials.bukkit.time

import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import org.bukkit.scheduler.BukkitRunnable

class TimeSlowRunnable(private val plugin: RPKEssentialsBukkit) : BukkitRunnable() {

    override fun run() {
        for (world in plugin.server.worlds) {
            world.fullTime = (world.fullTime - 100L + 100L * (1 / plugin.config.getDouble("time-slow-factor"))).toLong()
        }
    }

}