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

package com.seventh_root.elysium.chat.bukkit.prefix

import com.seventh_root.elysium.core.database.Entity

/**
 * Represents a prefix.
 */
interface ElysiumPrefix: Entity {

    /**
     * The name of the prefix.
     * This is used to create the permissions, and for purposes of configuration.
     */
    val name: String

    /**
     * The prefix as it appears in chat.
     * May use Minecraft chat colours.
     */
    var prefix: String
}