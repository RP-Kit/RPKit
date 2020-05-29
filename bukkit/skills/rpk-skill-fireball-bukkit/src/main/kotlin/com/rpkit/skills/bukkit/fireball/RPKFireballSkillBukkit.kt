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

package com.rpkit.skills.bukkit.fireball

import com.rpkit.core.bukkit.event.provider.RPKBukkitServiceProviderReadyEvent
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.exception.UnregisteredServiceException
import com.rpkit.skills.bukkit.skills.RPKSkillProvider
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RPKFireballSkillBukkit : RPKBukkitPlugin(), Listener {

    private var registered = false

    override fun onEnable() {
        saveDefaultConfig()
    }

    override fun onPostEnable() {
        attemptSkillRegistration()
    }

    override fun registerListeners() {
        registerListeners(this)
    }

    @EventHandler
    fun onServiceProviderReady(event: RPKBukkitServiceProviderReadyEvent) {
        attemptSkillRegistration()
    }

    fun attemptSkillRegistration() {
        if (registered) return
        try {
            val skillProvider = core.serviceManager.getServiceProvider(RPKSkillProvider::class)
            skillProvider.addSkill(FireballSkill(this))
            registered = true
        } catch (ignored: UnregisteredServiceException) {}
    }

}