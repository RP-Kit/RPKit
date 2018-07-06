package com.rpkit.drinks.bukkit.database.table

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.drinks.bukkit.RPKDrinksBukkit
import com.rpkit.drinks.bukkit.database.jooq.rpkit.Tables.RPKIT_DRUNKENNESS
import com.rpkit.drinks.bukkit.drink.RPKDrunkenness
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType


class RPKDrunkennessTable(database: Database, private val plugin: RPKDrinksBukkit): Table<RPKDrunkenness>(database, RPKDrunkenness::class) {

    private val cache = database.cacheManager.createCache("rpk-drinks-bukkit.rpkit_drunkenness.id",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKDrunkenness::class.java,
                    ResourcePoolsBuilder.heap(plugin.server.maxPlayers.toLong())))

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_DRUNKENNESS)
                .column(RPKIT_DRUNKENNESS.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_DRUNKENNESS.CHARACTER_ID, SQLDataType.INTEGER)
                .column(RPKIT_DRUNKENNESS.DRUNKENNESS, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_drunkenness").primaryKey(RPKIT_DRUNKENNESS.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.1.0")
        }
    }

    override fun insert(entity: RPKDrunkenness): Int {
        database.create
                .insertInto(
                        RPKIT_DRUNKENNESS,
                        RPKIT_DRUNKENNESS.CHARACTER_ID,
                        RPKIT_DRUNKENNESS.DRUNKENNESS
                )
                .values(
                        entity.character.id,
                        entity.drunkenness
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        cache.put(id, entity)
        return id
    }

    override fun update(entity: RPKDrunkenness) {
        database.create
                .update(RPKIT_DRUNKENNESS)
                .set(RPKIT_DRUNKENNESS.CHARACTER_ID, entity.character.id)
                .set(RPKIT_DRUNKENNESS.DRUNKENNESS, entity.drunkenness)
                .where(RPKIT_DRUNKENNESS.ID.eq(entity.id))
                .execute()
        cache.put(entity.id, entity)
    }

    override fun get(id: Int): RPKDrunkenness? {
        if (cache.containsKey(id)) {
            return cache[id]
        } else {
            val result = database.create
                    .select(
                            RPKIT_DRUNKENNESS.CHARACTER_ID,
                            RPKIT_DRUNKENNESS.DRUNKENNESS
                    )
                    .from(RPKIT_DRUNKENNESS)
                    .where(RPKIT_DRUNKENNESS.ID.eq(id))
                    .fetchOne() ?: return null
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val characterId = result.get(RPKIT_DRUNKENNESS.CHARACTER_ID)
            val character = characterProvider.getCharacter(characterId)
            if (character != null) {
                val drunkenness = RPKDrunkenness(
                        id,
                        character,
                        result.get(RPKIT_DRUNKENNESS.DRUNKENNESS)
                )
                cache.put(id, drunkenness)
                return drunkenness
            } else {
                database.create
                        .deleteFrom(RPKIT_DRUNKENNESS)
                        .where(RPKIT_DRUNKENNESS.ID.eq(id))
                        .execute()
                cache.remove(id)
                return null
            }
        }
    }

    fun get(character: RPKCharacter): RPKDrunkenness? {
        val result = database.create
                .select(RPKIT_DRUNKENNESS.ID)
                .from(RPKIT_DRUNKENNESS)
                .where(RPKIT_DRUNKENNESS.CHARACTER_ID.eq(character.id))
                .fetchOne() ?: return null
        return get(result[RPKIT_DRUNKENNESS.ID])
    }

    override fun delete(entity: RPKDrunkenness) {
        database.create
                .deleteFrom(RPKIT_DRUNKENNESS)
                .where(RPKIT_DRUNKENNESS.ID.eq(entity.id))
                .execute()
        cache.remove(entity.id)
    }

}