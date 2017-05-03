package com.rpkit.players.bukkit.profile

import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


class RPKProfileImpl: RPKProfile {

    override var id: Int = 0
    override var name: String
    override var passwordHash: ByteArray
    override var passwordSalt: ByteArray

    constructor(id: Int, name: String, passwordHash: ByteArray, passwordSalt: ByteArray) {
        this.id = id
        this.name = name
        this.passwordHash = passwordHash
        this.passwordSalt = passwordSalt
    }

    constructor(name: String, password: String) {
        this.id = 0
        this.name = name
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