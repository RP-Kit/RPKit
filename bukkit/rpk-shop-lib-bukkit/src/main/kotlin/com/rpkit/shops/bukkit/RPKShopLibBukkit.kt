/*
 * Copyright 2016 Ross Binden
 *
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

package com.rpkit.shops.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bstats.bukkit.Metrics

/**
 * Class to allow shop lib to load as a plugin.
 * This allows plugins requiring shops or implementing shops to depend on the plugin.
 */
class RPKShopLibBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        Metrics(this)
        serviceProviders = arrayOf()
    }

}
