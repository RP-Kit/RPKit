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

package com.rpkit.players.bukkit.profile

import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


class RPKProfileImpl : RPKProfile {

    override var id: Int? = null
    override var name: String
    override var discriminator: Int
    override var passwordHash: ByteArray
    override var passwordSalt: ByteArray

    constructor(id: Int, name: String, discriminator: Int, passwordHash: ByteArray, passwordSalt: ByteArray) {
        this.id = id
        this.name = name
        this.discriminator = discriminator
        this.passwordHash = passwordHash
        this.passwordSalt = passwordSalt
    }

    constructor(name: String, discriminator: Int, password: String) {
        this.id = 0
        this.name = name
        this.discriminator = discriminator
        val random = SecureRandom()
        passwordSalt = ByteArray(16)
        random.nextBytes(passwordSalt)
        val spec = PBEKeySpec(password.toCharArray(), passwordSalt, 65536, 128)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        passwordHash = factory.generateSecret(spec).encoded
    }

    override fun setPassword(password: CharArray) {
        val random = SecureRandom()
        passwordSalt = ByteArray(16)
        random.nextBytes(passwordSalt)
        val spec = PBEKeySpec(password, passwordSalt, 65536, 128)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        passwordHash = factory.generateSecret(spec).encoded
    }

    override fun checkPassword(password: CharArray): Boolean {
        val spec = PBEKeySpec(password, passwordSalt, 65536, 128)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512")
        return Arrays.equals(passwordHash, factory.generateSecret(spec).encoded)
    }
}