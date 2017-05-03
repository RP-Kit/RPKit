package com.rpkit.players.bukkit.profile

import com.rpkit.core.database.Entity


interface RPKProfile: Entity {

    var name: String
    var passwordHash: ByteArray
    var passwordSalt: ByteArray
    fun setPassword(password: CharArray)
    fun checkPassword(password: CharArray): Boolean

}