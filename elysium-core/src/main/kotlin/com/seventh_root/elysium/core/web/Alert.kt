/*
 * Copyright 2016 Ross Binden
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

package com.seventh_root.elysium.core.web

/**
 * Represents an alert, to be shown on a page
 *
 * @property type The type of the alert. This will affect which colour it shows in.
 * @property message The message shown in the alert.
 */
class Alert(
        val type: Alert.Type,
        val message: String
) {

    /**
     * An enum containing all types of alerts.
     * These are applied to alerts with bootstrap CSS classes.
     *
     * @property cssClass The bootstrap CSS class.
     */
    enum class Type(val cssClass: String) {

        /**
         * An alert type for an operation that ended in success.
         */
        SUCCESS("alert-success"),

        /**
         * An informative alert type.
         */
        INFO("alert-info"),

        /**
         * A warning alert type.
         */
        WARNING("alert-warning"),

        /**
         * An alert type which indicates danger.
         */
        DANGER("alert-danger");

        /**
         * Converts the type to a string containing the CSS class to use with bootstrap.
         */
        override fun toString(): String {
            return cssClass
        }
    }

}