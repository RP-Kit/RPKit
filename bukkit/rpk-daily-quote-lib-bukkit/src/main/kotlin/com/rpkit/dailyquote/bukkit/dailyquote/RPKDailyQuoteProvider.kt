package com.rpkit.dailyquote.bukkit.dailyquote

import com.rpkit.core.service.ServiceProvider


interface RPKDailyQuoteProvider: ServiceProvider {

    fun getDailyQuote(): String

}