package com.rpkit.warp.bukkit.warp

import com.rpkit.core.service.ServiceProvider


interface RPKWarpProvider: ServiceProvider {

    val warps: List<RPKWarp>
    fun getWarp(id: Int): RPKWarp?
    fun getWarp(name: String): RPKWarp?
    fun addWarp(warp: RPKWarp)
    fun updateWarp(warp: RPKWarp)
    fun removeWarp(warp: RPKWarp)

}