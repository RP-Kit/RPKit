package com.rpkit.locks.bukkit.lock

import com.rpkit.core.service.ServiceProvider
import com.rpkit.players.bukkit.player.RPKPlayer
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

/**
 * Provides lock related operations.
 */
interface RPKLockProvider : ServiceProvider {

    /**
     * The item used to represent a lock.
     * This should have item meta to distinguish it from other items.
     */
    val lockItem: ItemStack

    /**
     * Checks whether a block is locked.
     *
     * @param block The block
     * @return Whether the block is locked
     */
    fun isLocked(block: Block): Boolean

    /**
     * Sets a block to be locked.
     *
     * @param block The block
     * @param locked Whether the block should be locked
     */
    fun setLocked(block: Block, locked: Boolean)

    /**
     * Checks whether a player is currently locking a block.
     * When a player is claiming, the next block they interact with becomes locked, and they are given a key item.
     *
     * @param player The player
     * @return Whether the player is claiming
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("isClaiming(minecraftProfile)"))
    fun isClaiming(player: RPKPlayer): Boolean

    /**
     * Checks whether a Minecraft profile is currently locking a block.
     * When a Minecraft profile is claiming, the next block they interact with becomes locked, and they are given a key
     * item.
     *
     * @param minecraftProfile The Minecraft profile
     * @return Whether the Minecraft profile is claiming
     */
    fun isClaiming(minecraftProfile: RPKMinecraftProfile): Boolean

    /**
     * Checks whether a player is currently unlocking a block.
     * When a player is unclaiming, the next block they interact with becomes unlocked, the key is removed from their
     * inventory and keyring if present, and a lock is returned to them.
     *
     * @param player The player
     * @return Whether the player is unclaiming
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("isUnclaiming(minecraftProfile)"))
    fun isUnclaiming(player: RPKPlayer): Boolean

    /**
     * Checks whether a Minecraft profile is currently unlocking a block.
     * When a Minecraft profile is unclaiming, the next block they interact with becomes unlocked, the key is removed from
     * their inventory and keyring if present, and a lock is returned to them.
     *
     * @param minecraftProfile The Minecraft profile
     * @return Whether the Minecraft profile is unclaiming
     */
    fun isUnclaiming(minecraftProfile: RPKMinecraftProfile): Boolean

    /**
     * Sets whether a player is currently unlocking a block.
     * When a player is unclaiming, the next block they interact with becomes unlocked, the key is removed from their
     * inventory and keyring if present, and a lock is returned to them.
     *
     * @param player The player
     * @param unclaiming Whether the player should be unclaiming
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("setUnclaiming(minecraftProfile, unclaiming)"))
    fun setUnclaiming(player: RPKPlayer, unclaiming: Boolean)

    /**
     * Sets whether a Minecraft profile is currently unlocking a block.
     * When a player is unclaiming, the next block they interact with becomes unlocked, the key is removed from their
     * inventory and keyring if present, and a lock is returned o them.
     *
     * @param minecraftProfile The Minecraft profile
     * @param unclaiming Whether the Minecraft profile should be unclaiming
     */
    fun setUnclaiming(minecraftProfile: RPKMinecraftProfile, unclaiming: Boolean)

    /**
     * Checks whether a player is currently getting a key.
     * When a player is getting a key, if the next block they interact with is locked, they receive the key for the
     * block.
     *
     * @param player The player
     * @return Whether the player is getting a key
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("isGettingKey(minecraftProfile)"))
    fun isGettingKey(player: RPKPlayer): Boolean

    /**
     * Checks whether a Minecraft profile is currently getting a key.
     * When a Minecraft profile is getting a key, if the next block they interact with is locked, they receive the key
     * for the block.
     *
     * @param minecraftProfile The Minecraft profile
     * @return Whether the Minecraft profile is getting a key
     */
    fun isGettingKey(minecraftProfile: RPKMinecraftProfile): Boolean

    /**
     * Sets whether a player is currently getting a key.
     * When a player is getting a key, if the next block they interact with is locked, they receive the key for the
     * block.
     *
     * @param player The player
     * @param gettingKey Whether the player should be getting a key
     */
    @Deprecated("Old players API. Please move to new profiles APIs.", ReplaceWith("setGettingKey(minecraftProfile, gettingKey)"))
    fun setGettingKey(player: RPKPlayer, gettingKey: Boolean)

    /**
     * Sets whether a Minecraft profile is currently getting a key.
     * When a Minecraft profile is getting a key, if the next block they interact with is locked, they receive the key
     * for the block.
     *
     * @param minecraftProfile The Minecraft profile
     * @param gettingKey Whether the Minecraft profile should be getting a key
     */
    fun setGettingKey(minecraftProfile: RPKMinecraftProfile, gettingKey: Boolean)

    /**
     * Gets the key item for a block, complete with location-specific lore.
     *
     * @param block The block to get the key item for
     * @return The key item
     */
    fun getKeyFor(block: Block): ItemStack

    /**
     * Checks whether an item is a key
     *
     * @param item The item to check
     * @return Whether the item is a key
     */
    fun isKey(item: ItemStack): Boolean

}