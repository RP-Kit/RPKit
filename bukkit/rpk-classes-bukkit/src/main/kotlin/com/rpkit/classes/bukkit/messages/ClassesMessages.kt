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

package com.rpkit.classes.bukkit.messages

import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage

class ClassesMessages(plugin: RPKClassesBukkit) : BukkitMessages(plugin) {

    class ClassSetInvalidAgeMessage(private val message: ParameterizedMessage) {
        fun withParameters(maxAge: Int, minAge: Int) =
            message.withParameters(mapOf("min_age" to minAge.toString(), "max_age" to maxAge.toString()))
    }

    val classSetInvalidAge = getParameterized("class-set-invalid-age")
        .let(::ClassSetInvalidAgeMessage)
}
