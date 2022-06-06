/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.notifications.bukkit.listener

import com.rpkit.notifications.bukkit.RPKNotificationsBukkit
import com.rpkit.notifications.bukkit.database.table.RPKNotificationTable
import com.rpkit.players.bukkit.event.profile.RPKBukkitProfileDeleteEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

class RPKProfileDeleteListener(private val plugin: RPKNotificationsBukkit) : Listener {

    @EventHandler
    fun onProfileDelete(event: RPKBukkitProfileDeleteEvent) {
        val profileId = event.profile.id
        if (profileId != null) {
            plugin.database.getTable(RPKNotificationTable::class.java).delete(profileId)
        }
    }

}