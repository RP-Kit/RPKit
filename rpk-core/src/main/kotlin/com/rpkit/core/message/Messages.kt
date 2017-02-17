package com.rpkit.core.message

/**
 * Interface for retrieving messages.
 */
interface Messages {

    /**
     * Gets a message, replacing variables using the provided map.
     *
     * @param key The message key, this is used to look up the message
     * @param vars A map of variables to their replacements in the message
     * @return The message
     */
    operator fun get(key: String, vars: Map<String, String>): String

    /**
     * Gets a message
     *
     * @param key The message key, this is used to look up the message
     * @return The message
     */
    operator fun get(key: String): String

    /**
     * Sets a message
     *
     * @param key The message key, this is used to look up the message
     * @param value The message, with variables represented with '$'
     */
    operator fun set(key: String, value: String)

    /**
     * Sets a message if it has not yet been set.
     * This is used for setting plugin defaults while not overriding configured values.
     *
     * @param key The message key, this is used to look up the message
     * @param value The message, with variables represented with '$'
     */
    fun setDefault(key: String, value: String)

}