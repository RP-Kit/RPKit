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

package com.rpkit.moderation.bukkit.warning

import com.rpkit.players.bukkit.profile.RPKProfile
import java.time.LocalDateTime


class RPKWarningImpl(
        override var id: Int = 0,
        override val reason: String,
        override val profile: RPKProfile,
        override val issuer: RPKProfile,
        override val time: LocalDateTime
): RPKWarning {
    constructor(reason: String, player: RPKProfile, issuer: RPKProfile): this(0, reason, player, issuer, LocalDateTime.now())
}