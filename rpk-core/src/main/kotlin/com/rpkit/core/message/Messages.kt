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
     * Gets a parameterized message
     *
     * @param key The message key, this is used to lookup the message
     * @return The parameterized message. This requires parameters to turn the template into the final message shown.
     */
    fun getParameterized(key: String): ParameterizedMessage

    /**
     * Gets a list of messages, replacing variables using the provided map.
     *
     * @param key The message key, this is used to look up the message list
     * @param vars A map of variables to their replacements in the message list
     * @return The message list
     */
    fun getList(key: String, vars: Map<String, String>): List<String>

    /**
     * Gets a list of messages.
     *
     * @param key The message key, this is used to look up the message list
     * @return The message list
     */
    fun getList(key: String): List<String>

    /**
     * Gets a parameterized list of messages
     *
     * @param key The message key, this is used to look up the message list
     * @return A list of parameterized messages
     */
    fun getParameterizedList(key: String): List<ParameterizedMessage>

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

    /**
     * Sets a list of messages if it has not yet been set.
     * This is used for setting plugin defaults while not overriding configured values.
     *
     * @param key The message key, this is used to look up the message list
     * @param value The message list
     */
    fun setDefault(key: String, value: List<String>)

}