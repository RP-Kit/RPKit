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

package com.rpkit.core.bukkit.util

import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Converts the [ItemStack] to a [ByteArray] for serialization.
 *
 * @return The [ByteArray]
 */
fun ItemStack.toByteArray(): ByteArray {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        BukkitObjectOutputStream(byteArrayOutputStream).use { bukkitObjectOutputStream ->
            bukkitObjectOutputStream.writeObject(this)
            return byteArrayOutputStream.toByteArray()
        }
    }
}

/**
 * Converts the given byte array to an [ItemStack].
 *
 * @param bytes The [ByteArray]
 * @return The [ItemStack]
 * @deprecated Replaced by extension method
 * @see toItemStack
 */
fun itemStackFromByteArray(bytes: ByteArray): ItemStack {
    return bytes.toItemStack()
}

/**
 * Converts the byte array to an [ItemStack].
 *
 * @return The [ItemStack]
 */
fun ByteArray.toItemStack(): ItemStack {
    ByteArrayInputStream(this).use { byteArrayInputStream ->
        BukkitObjectInputStream(byteArrayInputStream).use { bukkitObjectInputStream ->
            return bukkitObjectInputStream.readObject() as ItemStack
        }
    }
}

/**
 * Converts the [ItemStack] [Array] to a [ByteArray] for serialization.
 *
 * @return The [ByteArray]
 */
fun Array<ItemStack>.toByteArray(): ByteArray {
    ByteArrayOutputStream().use { byteArrayOutputStream ->
        BukkitObjectOutputStream(byteArrayOutputStream).use { bukkitObjectOutputStream ->
            bukkitObjectOutputStream.writeObject(this)
            return byteArrayOutputStream.toByteArray()
        }
    }
}

/**
 * Converts the [ByteArray] to an [ItemStack] [Array]
 *
 * @return The [ItemStack] [Array]
 * @deprecated Replaced by extension method
 * @see toItemStackArray
 */
fun itemStackArrayFromByteArray(bytes: ByteArray): Array<ItemStack> {
    return bytes.toItemStackArray()
}

/**
 * Converts the [ByteArray] to an [ItemStack] [Array]
 *
 * @return The [ItemStack] [Array]
 */
fun ByteArray.toItemStackArray(): Array<ItemStack> {
    ByteArrayInputStream(this).use { byteArrayInputStream ->
        BukkitObjectInputStream(byteArrayInputStream).use { bukkitObjectInputStream ->
            return bukkitObjectInputStream.readObject() as Array<ItemStack>
        }
    }
}

/**
 * Creates a clone of the [ItemStack] with the given display name
 *
 * @return A clone of the [ItemStack] with the given display name
 */
fun ItemStack.withDisplayName(displayName: String): ItemStack {
    val newItemStack = clone()
    val meta = newItemStack.itemMeta ?: return newItemStack
    meta.setDisplayName(displayName)
    newItemStack.itemMeta = meta
    return newItemStack
}

/**
 * Creates a clone of the [ItemStack] with the given lore.
 * If the [ItemStack] already has lore the lore is appended to the existing lore.
 *
 * @return A clone of the [ItemStack] with the given lore
 */
fun ItemStack.withLore(lore: List<String>): ItemStack {
    val newItemStack = clone()
    val meta = newItemStack.itemMeta ?: return newItemStack
    val metaLore = meta.lore ?: mutableListOf()
    metaLore.addAll(lore)
    meta.lore = metaLore
    newItemStack.itemMeta = meta
    return newItemStack
}

/**
 * Creates a clone of the [ItemStack] with the given lore removed.
 *
 * @return A clone of the [ItemStack] with the given lore removed.
 */
fun ItemStack.withoutLore(lore: List<String>): ItemStack {
    val newItemStack = clone()
    val meta = newItemStack.itemMeta ?: return newItemStack
    val metaLore = meta.lore ?: mutableListOf()
    metaLore.removeAll(lore)
    return newItemStack
}

/**
 * Creates a clone of the [ItemStack] with any lore matching the given [Regex] removed.
 *
 * @return A clone of the [ItemStack] with any lore matching the given [Regex] removed.
 */
fun ItemStack.withoutLoreMatching(regex: Regex): ItemStack {
    val newItemStack = clone()
    val meta = newItemStack.itemMeta ?: return newItemStack
    val metaLore = meta.lore?.filter { loreItem -> !loreItem.matches(regex) } ?: mutableListOf()
    meta.lore = metaLore
    return newItemStack
}
