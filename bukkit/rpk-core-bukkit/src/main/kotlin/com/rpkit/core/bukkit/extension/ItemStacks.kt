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

package com.rpkit.core.bukkit.extension

import com.rpkit.core.bukkit.reflect.ReflectionUtil
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.lang.reflect.Method
import java.util.logging.Level.SEVERE


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
fun Array<out ItemStack?>.toByteArray(): ByteArray {
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
 */
fun ByteArray.toItemStackArray(): Array<ItemStack?> {
    ByteArrayInputStream(this).use { byteArrayInputStream ->
        BukkitObjectInputStream(byteArrayInputStream).use { bukkitObjectInputStream ->
            return bukkitObjectInputStream.readObject() as Array<ItemStack?>
        }
    }
}

/**
 * Creates a clone of the [ItemStack] with the given display name
 *
 * @param displayName The display name to set
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
fun ItemStack.withoutLoreMatching(regex: String): ItemStack {
    val newItemStack = clone()
    val meta = newItemStack.itemMeta ?: return newItemStack
    val metaLore = meta.lore?.filter { loreItem -> !loreItem.matches(Regex(regex)) } ?: mutableListOf()
    meta.lore = metaLore
    newItemStack.itemMeta = meta
    return newItemStack
}

/**
 * Sets the display name of the [ItemStack]
 *
 * @param name The display name to set
 */
fun ItemStack.setDisplayName(name: String) {
    val meta = itemMeta ?: return
    meta.setDisplayName(name)
    itemMeta = meta
}

/**
 * Adds lore to the item stack
 *
 * @param lore The lore to add
 */
fun ItemStack.addLore(lore: List<String>) {
    val meta = itemMeta ?: return
    val metaLore = meta.lore ?: mutableListOf()
    metaLore.addAll(lore)
    meta.lore = metaLore
    itemMeta = meta
}

/**
 * Removes lore from the [ItemStack]
 *
 * @param lore The lore to remove
 */
fun ItemStack.removeLore(lore: List<String>) {
    val meta = itemMeta ?: return
    val metaLore = meta.lore ?: mutableListOf()
    metaLore.removeAll(lore)
    meta.lore = metaLore
    itemMeta = meta
}

/**
 * Converts an [org.bukkit.inventory.ItemStack] to a Json string
 * for sending with [net.md_5.bungee.api.chat.BaseComponent]'s.
 *
 * @receiver the item to convert
 * @return the Json string representation of the item
 */
fun ItemStack.toNbtJson(): String? {
    // ItemStack methods to get a net.minecraft.server.ItemStack object for serialization
    val craftItemStackClazz: Class<*> = ReflectionUtil.getOBCClass("inventory.CraftItemStack") ?: return null
    val asNMSCopyMethod: Method = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack::class.java) ?: return null

    // NMS Method to serialize a net.minecraft.server.ItemStack to a valid Json string
    val nmsItemStackClazz: Class<*> = ReflectionUtil.getNMSClass("world.item.ItemStack") ?: return null
    val nbtTagCompoundClazz: Class<*> = ReflectionUtil.getNMSClass("nbt.NBTTagCompound") ?: return null
    val saveNmsItemStackMethod: Method = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz) ?: return null
    val nmsNbtTagCompoundObj: Any? // This will just be an empty NBTTagCompound instance to invoke the saveNms method
    val nmsItemStackObj: Any? // This is the net.minecraft.server.ItemStack object received from the asNMSCopy method
    val itemAsJsonObject: Any? // This is the net.minecraft.server.ItemStack after being put through saveNmsItem method
    try {
        nmsNbtTagCompoundObj = nbtTagCompoundClazz.getDeclaredConstructor()?.newInstance() ?: return null
        nmsItemStackObj = asNMSCopyMethod(null, this)
        itemAsJsonObject = saveNmsItemStackMethod(nmsItemStackObj, nmsNbtTagCompoundObj)
    } catch (t: Throwable) {
        Bukkit.getLogger().log(SEVERE, "failed to serialize itemstack to nms item", t)
        return null
    }

    // Return a string representation of the serialized object
    return itemAsJsonObject.toString()
}

fun ItemStack.tagToNbtJson(): String? {
    // ItemStack methods to get a net.minecraft.server.ItemStack object for serialization
    val craftItemStackClazz: Class<*> = ReflectionUtil.getOBCClass("inventory.CraftItemStack") ?: return null
    val asNMSCopyMethod: Method = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack::class.java) ?: return null

    // NMS Method to serialize a net.minecraft.server.ItemStack to a valid Json string
    val nmsItemStackClazz: Class<*> = ReflectionUtil.getNMSClass("world.item.ItemStack") ?: return null
    val nbtTagCompoundClazz: Class<*> = ReflectionUtil.getNMSClass("nbt.NBTTagCompound") ?: return null
    val nmsGetTagMethod: Method = ReflectionUtil.getMethod(nmsItemStackClazz, "t") ?: return null
    val nmsItemStackObj: Any? // This is the net.minecraft.server.ItemStack object received from the asNMSCopy method
    val itemTagAsJsonObject: Any? // This is the net.minecraft.server.ItemStack after being put through saveNmsItem method
    try {
        nmsItemStackObj = asNMSCopyMethod(null, this)
        itemTagAsJsonObject = nmsGetTagMethod(nmsItemStackObj) ?: nbtTagCompoundClazz.getDeclaredConstructor().newInstance()
    } catch (t: Throwable) {
        Bukkit.getLogger().log(SEVERE, "failed to serialize itemstack to nms item", t)
        return null
    }

    // Return a string representation of the serialized object
    return itemTagAsJsonObject.toString()
}
