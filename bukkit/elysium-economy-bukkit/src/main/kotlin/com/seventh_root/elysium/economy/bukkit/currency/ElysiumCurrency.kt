package com.seventh_root.elysium.economy.bukkit.currency

import com.seventh_root.elysium.core.database.TableRow
import org.bukkit.Material


class ElysiumCurrency(
        override var id: Int = 0,
        var name: String,
        var nameSingular: String,
        var namePlural: String,
        var rate: Double,
        var defaultAmount: Int,
        var material: Material
): TableRow {
    fun convert(amount: Double, currency: ElysiumCurrency): Double {
        return (amount / rate) * currency.rate;
    }
}