package com.rpkit.rolling.bukkit

import java.util.*


class Die(val sides: Int) {

    private val random = Random()

    fun roll(): Int {
        return random.nextInt(sides) + 1
    }

}