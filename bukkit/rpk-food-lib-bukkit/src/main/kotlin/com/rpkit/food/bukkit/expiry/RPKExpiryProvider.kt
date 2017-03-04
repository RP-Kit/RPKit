package com.rpkit.food.bukkit.expiry

import com.rpkit.core.service.ServiceProvider
import org.bukkit.inventory.ItemStack
import java.util.*

interface RPKExpiryProvider: ServiceProvider {

    fun setExpiry(item: ItemStack, expiryDate: Date)
    fun setExpiry(item: ItemStack)
    fun getExpiry(item: ItemStack): Date?
    fun isExpired(item: ItemStack): Boolean

}
