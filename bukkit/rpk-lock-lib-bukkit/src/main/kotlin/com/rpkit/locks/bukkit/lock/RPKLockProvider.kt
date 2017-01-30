package com.rpkit.locks.bukkit.lock

import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.player.RPKPlayer
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack


interface RPKLockProvider : ServiceProvider {

    val lockItem: ItemStack
    fun isLocked(block: Block): Boolean
    fun setLocked(block: Block, locked: Boolean)
    fun isClaiming(player: RPKPlayer): Boolean
    fun isUnclaiming(player: RPKPlayer): Boolean
    fun setUnclaiming(player: RPKPlayer, unclaiming: Boolean)
    fun isGettingKey(player: RPKPlayer): Boolean
    fun setGettingKey(player: RPKPlayer, gettingKey: Boolean)
    fun getKeyFor(block: Block): ItemStack
    fun isKey(item: ItemStack): Boolean

}