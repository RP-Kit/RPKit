package com.seventh_root.elysium.api.economy

import com.seventh_root.elysium.core.database.TableRow


open class ElysiumCurrency(
        override var id: Int,
        val name: String,
        val nameSingular: String,
        val namePlural: String,
        var rate: Double,
        var defaultAmount: Int
): TableRow {

    fun convert(amount: Double, currency: ElysiumCurrency): Double {
        return (amount / rate) * currency.rate;
    }

}