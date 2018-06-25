/*
 * Copyright 2018 Ross Binden
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

package com.rpkit.moderation.bukkit.ticket

import com.rpkit.core.database.Entity
import com.rpkit.players.bukkit.profile.RPKProfile
import org.bukkit.Location
import java.time.LocalDateTime


interface RPKTicket: Entity {

    val reason: String
    val issuer: RPKProfile
    var resolver: RPKProfile?
    val location: Location?
    val openDate: LocalDateTime
    var closeDate: LocalDateTime?
    var isClosed: Boolean

    fun close(resolver: RPKProfile)

}