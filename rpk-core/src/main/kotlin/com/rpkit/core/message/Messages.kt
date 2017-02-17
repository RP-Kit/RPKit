package com.rpkit.core.message


interface Messages {

    operator fun get(key: String, vars: Map<String, String>): String
    operator fun set(key: String, value: String)
    fun setDefault(key: String, value: String)

}