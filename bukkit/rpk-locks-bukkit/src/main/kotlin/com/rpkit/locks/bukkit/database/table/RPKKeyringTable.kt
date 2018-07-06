package com.rpkit.locks.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.bukkit.util.toByteArray
import com.rpkit.core.bukkit.util.toItemStackArray
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.jooq.rpkit.Tables.RPKIT_KEYRING
import com.rpkit.locks.bukkit.keyring.RPKKeyring
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType


class RPKKeyringTable(database: Database, private val plugin: RPKLocksBukkit): Table<RPKKeyring>(database, RPKKeyring::class) {

    private val cache = database.cacheManager.createCache("rpk-locks-bukkit.rpkit_keyring.id",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKKeyring::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_KEYRING)
                .column(RPKIT_KEYRING.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_KEYRING.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_KEYRING.ITEMS, SQLDataType.BLOB)
                .constraints(
                        constraint("pk_rpkit_keyring").primaryKey(RPKIT_KEYRING.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.1.0")
        }
    }

    override fun insert(entity: RPKKeyring): Int {
        database.create
                .insertInto(
                        RPKIT_KEYRING,
                        RPKIT_KEYRING.CHARACTER_ID,
                        RPKIT_KEYRING.ITEMS
                )
                .values(
                        entity.character.id,
                        entity.items.toTypedArray().toByteArray()
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKKeyring) {
        database.create
                .update(RPKIT_KEYRING)
                .set(RPKIT_KEYRING.CHARACTER_ID, entity.character.id)
                .set(RPKIT_KEYRING.ITEMS, entity.items.toTypedArray().toByteArray())
                .where(RPKIT_KEYRING.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKKeyring? {
        if (cache.containsKey(id)) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_KEYRING.CHARACTER_ID,
                            RPKIT_KEYRING.ITEMS
                    )
                    .from(RPKIT_KEYRING)
                    .where(RPKIT_KEYRING.ID.eq(id))
                    .fetchOne() ?: return null
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val characterId = result.get(RPKIT_KEYRING.CHARACTER_ID)
            val character = characterProvider.getCharacter(characterId)
            if (character != null) {
                val keyring = RPKKeyring(
                        id,
                        character,
                        result.get(RPKIT_KEYRING.ITEMS).toItemStackArray().toMutableList()
                )
                cache.put(id, keyring)
                return keyring
            } else {
                database.create
                        .deleteFrom(RPKIT_KEYRING)
                        .where(RPKIT_KEYRING.ID.eq(id))
                        .execute()
                cache.remove(id)
                return null
            }
        }
    }

    fun get(character: RPKCharacter): RPKKeyring? {
        val result = database.create
                .select(RPKIT_KEYRING.ID)
                .from(RPKIT_KEYRING)
                .where(RPKIT_KEYRING.CHARACTER_ID.eq(character.id))
                .fetchOne() ?: return null
        return get(result[RPKIT_KEYRING.ID])
    }

    override fun delete(entity: RPKKeyring) {
        database.create
                .deleteFrom(RPKIT_KEYRING)
                .where(RPKIT_KEYRING.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }
}