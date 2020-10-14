/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.core.bukkit.service

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.service.Service
import com.rpkit.core.service.ServicesDelegate
import org.bukkit.Bukkit
import org.bukkit.plugin.ServicePriority.Normal
import kotlin.reflect.KClass

class BukkitServicesDelegate : ServicesDelegate {

    override fun <T : Service> get(type: KClass<T>) =
            Bukkit.getServicesManager().getRegistration(type.java)?.provider

    override fun <T : Service> set(type: KClass<T>, service: T) {
        val plugin = service.plugin
        if (plugin is RPKBukkitPlugin) {
            Bukkit.getServicesManager().register(type.java, service, plugin, Normal)
        }
    }

}