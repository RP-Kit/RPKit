package com.rpkit.essentials.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.essentials.bukkit.database.jooq.rpkit.Tables.RPKIT_LOG_MESSAGES_ENABLED
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessagesEnabled
import com.rpkit.players.bukkit.profile.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.DSL.field
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType


class RPKLogMessagesEnabledTable(database: Database, private val plugin: RPKEssentialsBukkit): Table<RPKLogMessagesEnabled>(database, RPKLogMessagesEnabled::class) {

    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_LOG_MESSAGES_ENABLED)
                .column(RPKIT_LOG_MESSAGES_ENABLED.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                .column(RPKIT_LOG_MESSAGES_ENABLED.ENABLED, SQLDataType.TINYINT.length(1))
                .constraints(
                        constraint("pk_rpkit_log_messages_enabled").primaryKey(RPKIT_LOG_MESSAGES_ENABLED.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
        if (database.getTableVersion(this) == "1.1.0") {
            database.create
                    .truncate(RPKIT_LOG_MESSAGES_ENABLED)
                    .execute()
            database.create
                    .alterTable(RPKIT_LOG_MESSAGES_ENABLED)
                    .dropColumn(field("player_id"))
                    .execute()
            database.create
                    .alterTable(RPKIT_LOG_MESSAGES_ENABLED)
                    .addColumn(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID, SQLDataType.INTEGER)
                    .execute()
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKLogMessagesEnabled): Int {
        database.create
                .insertInto(
                        RPKIT_LOG_MESSAGES_ENABLED,
                        RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID,
                        RPKIT_LOG_MESSAGES_ENABLED.ENABLED
                )
                .values(
                        entity.minecraftProfile.id,
                        if (entity.enabled) 1.toByte() else 0.toByte()
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        return id
    }

    override fun update(entity: RPKLogMessagesEnabled) {
        database.create
                .update(RPKIT_LOG_MESSAGES_ENABLED)
                .set(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID, entity.minecraftProfile.id)
                .set(RPKIT_LOG_MESSAGES_ENABLED.ENABLED, if (entity.enabled) 1.toByte() else 0.toByte())
                .where(RPKIT_LOG_MESSAGES_ENABLED.ID.eq(entity.id))
                .execute()
    }

    override fun get(id: Int): RPKLogMessagesEnabled? {
        val result = database.create
                .select(
                        RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID,
                        RPKIT_LOG_MESSAGES_ENABLED.ENABLED
                )
                .from(RPKIT_LOG_MESSAGES_ENABLED)
                .where(RPKIT_LOG_MESSAGES_ENABLED.ID.eq(id))
                .fetchOne() ?: return null
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfileId = result.get(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(minecraftProfileId)
        if (minecraftProfile != null) {
            val logMessagesEnabled = RPKLogMessagesEnabled(
                    id,
                    minecraftProfile,
                    result.get(RPKIT_LOG_MESSAGES_ENABLED.ENABLED) == 1.toByte()
            )
            return logMessagesEnabled
        } else {
            database.create
                    .deleteFrom(RPKIT_LOG_MESSAGES_ENABLED)
                    .where(RPKIT_LOG_MESSAGES_ENABLED.ID.eq(id))
                    .execute()
            return null
        }
    }

    fun get(minecraftProfile: RPKMinecraftProfile): RPKLogMessagesEnabled? {
        val result = database.create
                .select(RPKIT_LOG_MESSAGES_ENABLED.ID)
                .from(RPKIT_LOG_MESSAGES_ENABLED)
                .where(RPKIT_LOG_MESSAGES_ENABLED.MINECRAFT_PROFILE_ID.eq(minecraftProfile.id))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_LOG_MESSAGES_ENABLED.ID))
    }

    override fun delete(entity: RPKLogMessagesEnabled) {
        database.create
                .deleteFrom(RPKIT_LOG_MESSAGES_ENABLED)
                .where(RPKIT_LOG_MESSAGES_ENABLED.ID.eq(entity.id))
                .execute()
    }

}