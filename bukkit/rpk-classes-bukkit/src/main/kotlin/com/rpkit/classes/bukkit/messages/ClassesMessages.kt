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

package com.rpkit.classes.bukkit.messages

import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClass
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to

class ClassesMessages(plugin: RPKClassesBukkit) : BukkitMessages(plugin) {

    class ClassSetInvalidAgeMessage(private val message: ParameterizedMessage) {
        fun withParameters(maxAge: Int, minAge: Int) =
            message.withParameters("min_age" to minAge.toString(), "max_age" to maxAge.toString())
    }

    class ClassSetValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(`class`: RPKClass) =
            message.withParameters(
                "class" to `class`.name.value
            )
    }

    class ClassListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(`class`: RPKClass) =
            message.withParameters(
                "class" to `class`.name.value
            )
    }

    val classUsage = get("class-usage")
    val noPermissionClassSet = get("no-permission-class-set")
    val classSetUsage = get("class-set-usage")
    val notFromConsole = get("not-from-console")
    val noCharacter = get("no-character")
    val classSetInvalidClass = get("class-set-invalid-class")
    val classSetInvalidPrerequisites = get("class-set-invalid-prerequisites")
    val classSetInvalidAge = getParameterized("class-set-invalid-age")
        .let(::ClassSetInvalidAgeMessage)
    val classSetValid = getParameterized("class-set-valid")
        .let(::ClassSetValidMessage)
    val noPermissionClassList = get("no-permission-class-list")
    val classListTitle = get("class-list-title")
    val classListItem = getParameterized("class-list-item")
        .let(::ClassListItemMessage)
    val noMinecraftProfile = get("no-minecraft-profile")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noCharacterService = get("no-character-service")
    val noClassService = get("no-class-service")
}
