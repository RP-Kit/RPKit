package com.rpkit.essentials.bukkit.kit

import com.rpkit.kit.bukkit.kit.RPKKit
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.inventory.ItemStack


class RPKKitImpl(
        override var id: Int,
        override val name: String,
        override val items: List<ItemStack>
) : RPKKit, ConfigurationSerializable {
    override fun serialize(): Map<String, Any> {
        return mapOf(
                Pair("id", id),
                Pair("name", name),
                Pair("items", items)
        )
    }

    companion object {
        @JvmStatic fun deserialize(serialized: Map<String, Any>): RPKKitImpl {
            return RPKKitImpl(
                    serialized["id"] as Int,
                    serialized["name"] as String,
                    serialized["items"] as List<ItemStack>
            )
        }
    }

}