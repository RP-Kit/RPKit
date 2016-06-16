package com.seventh_root.elysium.economy.bukkit.currency

import com.seventh_root.elysium.economy.bukkit.currency.ElysiumCurrency
import org.bukkit.Material


class BukkitCurrency(
        id: Int = 0,
        name: String,
        nameSingular: String,
        namePlural: String,
        rate: Double,
        defaultAmount: Int,
        val material: Material
) : ElysiumCurrency(
        id,
        name,
        nameSingular,
        namePlural,
        rate,
        defaultAmount
)