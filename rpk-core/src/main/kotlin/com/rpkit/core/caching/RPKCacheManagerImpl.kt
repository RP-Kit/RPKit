/*
 * Copyright 2022 Ren Binden
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

package com.rpkit.core.caching

internal class RPKCacheManagerImpl : RPKCacheManager {
    override fun <K : Any, V : Any> createCache(
        name: String,
        keyType: Class<K>,
        valueType: Class<V>,
        capacity: Long
    ): RPKCache<K, V> {
        return RPKCacheImpl(capacity)
    }

    override fun <K : Any, V : Any> createCache(config: RPKCacheConfiguration<K, V>): RPKCache<K, V> {
        return RPKCacheImpl(config.capacity)
    }
}