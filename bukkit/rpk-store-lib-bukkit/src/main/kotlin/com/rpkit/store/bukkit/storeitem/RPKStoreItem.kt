/*
 * Copyright 2018 Ross Binden
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

package com.rpkit.store.bukkit.storeitem

import com.rpkit.core.database.Entity

/**
 * Represents a store item.
 * This may be purchased by many different people.
 */
interface RPKStoreItem: Entity {

    /**
     * The plugin providing the store item
     * We store this as a string in case the plugin is uninstalled at some point after purchases have been made
     */
    val plugin: String

    /**
     * The unique identifier of the store item
     */
    val identifier: String

    /**
     * The description of the store item
     */
    val description: String

    /**
     * The cost of the store item
     */
    val cost: Int

}