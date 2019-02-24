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

import java.time.Duration

/**
 * Represents a timed store item.
 * A timed store item expires after a certain amount of time has passed.
 */
interface RPKTimedStoreItem: RPKStoreItem {

    /**
     * The duration for which the store item lasts.
     */
    val duration: Duration

}