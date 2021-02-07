/*
 * Copyright 2021 Ren Binden
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.warp.bukkit.warp

import com.rpkit.core.service.Service
import org.bukkit.Location


interface RPKWarpService : Service {

    val warps: List<RPKWarp>
    fun getWarp(name: RPKWarpName): RPKWarp?
    fun addWarp(warp: RPKWarp)
    fun createWarp(name: RPKWarpName, location: Location): RPKWarp
    fun updateWarp(warp: RPKWarp)
    fun removeWarp(warp: RPKWarp)

}