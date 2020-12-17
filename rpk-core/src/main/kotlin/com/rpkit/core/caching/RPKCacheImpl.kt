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

package com.rpkit.core.caching

import java.time.Instant

internal class RPKCacheImpl<K, V>(private val capacity: Long) : RPKCache<K, V> {

    private val records = mutableMapOf<K, RPKCacheRecord<V>>()

    private class RPKCacheRecord<V>(
        val value: V,
        var lastAccess: Instant = Instant.now()
    )

    override fun get(key: K): V? {
        val record = records[key] ?: return null
        record.lastAccess = Instant.now()
        return record.value
    }

    override fun set(key: K, value: V) {
        records[key] = RPKCacheRecord(value)
        while (records.size > capacity) {
            val oldestRecordKey = records.entries.minByOrNull { (_, record) -> record.lastAccess }?.key
            if (oldestRecordKey != null) records.remove(oldestRecordKey)
        }
    }

    override fun containsKey(key: K): Boolean {
        return records.containsKey(key)
    }

    override fun remove(key: K) {
        records.remove(key)
    }

    override fun keys(): Set<K> = records.keys
}