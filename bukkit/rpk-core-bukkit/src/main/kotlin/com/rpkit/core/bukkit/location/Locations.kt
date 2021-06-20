package com.rpkit.core.bukkit.location

import com.rpkit.core.location.RPKBlockLocation
import com.rpkit.core.location.RPKLocation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Block

fun RPKLocation.toBukkitLocation() = Bukkit.getWorld(world)?.let { world ->
    Location(world, x, y, z, yaw, pitch)
}

fun Location.toRPKLocation() = RPKLocation(
    world?.name ?: Bukkit.getWorlds()[0].name, x, y, z, yaw, pitch
)

fun RPKBlockLocation.toBukkitBlock() = Bukkit.getWorld(world)?.getBlockAt(x, y, z)

fun Location.toRPKBlockLocation() = RPKBlockLocation(
    world?.name ?: Bukkit.getWorlds()[0].name, blockX, blockY, blockZ
)

fun Block.toRPKBlockLocation() = RPKBlockLocation(
    world.name, x, y, z
)