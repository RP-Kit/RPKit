package com.rpkit.blocklog.bukkit

import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryProviderImpl
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin


class RPKBlockLoggingBukkit : RPKBukkitPlugin() {

    override fun onEnable() {
        serviceProviders = arrayOf(
                RPKBlockHistoryProviderImpl(this)
        )
    }

}