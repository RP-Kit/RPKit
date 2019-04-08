package com.rpkit.skills.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bstats.bukkit.Metrics

class RPKSkillLibBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        Metrics(this)
    }

}
