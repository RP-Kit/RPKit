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

package com.rpkit.locks.bukkit.messages

import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.locks.bukkit.RPKLocksBukkit
import org.bukkit.Material

class LocksMessages(plugin: RPKLocksBukkit) : BukkitMessages(plugin) {

    class BlockLockedMessage(private val message: ParameterizedMessage) {
        fun withParameters(blockType: Material) = message.withParameters(
            "block" to blockType.toString().lowercase().replace('_', ' ')
        )
    }

    val blockLocked = getParameterized("block-locked").let(::BlockLockedMessage)
    val craftingNoKeys = get("crafting-no-keys")
    val keyringInvalidItem = get("keyring-invalid-item")
    val lockSuccessful = get("lock-successful")
    val lockInvalidAlreadyLocked = get("lock-invalid-already-locked")
    val unlockSuccessful = get("unlock-successful")
    val unlockInvalidNoKey = get("unlock-invalid-no-key")
    val unlockInvalidNotLocked = get("unlock-invalid-not-locked")
    val getKeyInvalidNotLocked = get("get-key-invalid-not-locked")
    val getKeySuccessful = get("get-key-successful")
    val getKeyValid = get("get-key-valid")
    val unlockValid = get("unlock-valid")
    val copyKeyInvalidNoKeyInHand = get("copy-key-invalid-no-key-in-hand")
    val copyKeyInvalidNoMaterial = get("copy-key-invalid-no-material")
    val copyKeyValid = get("copy-key-valid")
    val notFromConsole = get("not-from-console")
    val noCharacter = get("no-character")
    val noMinecraftProfile = get("no-minecraft-profile")
    val noPermissionCopyKey = get("no-permission-copy-key")
    val noPermissionGetKey = get("no-permission-get-key")
    val noPermissionKeyring = get("no-permission-keyring")
    val noPermissionUnlock = get("no-permission-unlock")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noCharacterService = get("no-character-service")
    val noLockService = get("no-lock-service")
    val noKeyringService = get("no-keyring-service")
}