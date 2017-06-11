package com.rpkit.locks.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.jooq.rpkit.Tables.RPKIT_LOCKED_BLOCK
import com.rpkit.locks.bukkit.lock.RPKLockedBlock
import org.bukkit.block.Block
import org.jooq.SQLDialect
import org.jooq.impl.DSL.constraint
import org.jooq.impl.SQLDataType
import org.jooq.util.sqlite.SQLiteDataType


class RPKLockedBlockTable(database: Database, private val plugin: RPKLocksBukkit): Table<RPKLockedBlock>(database, RPKLockedBlock::class) {
    override fun create() {
        database.create
                .createTableIfNotExists(RPKIT_LOCKED_BLOCK)
                .column(RPKIT_LOCKED_BLOCK.ID, if (database.dialect == SQLDialect.SQLITE) SQLiteDataType.INTEGER.identity(true) else SQLDataType.INTEGER.identity(true))
                .column(RPKIT_LOCKED_BLOCK.WORLD, SQLDataType.VARCHAR(256))
                .column(RPKIT_LOCKED_BLOCK.X, SQLDataType.INTEGER)
                .column(RPKIT_LOCKED_BLOCK.Y, SQLDataType.INTEGER)
                .column(RPKIT_LOCKED_BLOCK.Z, SQLDataType.INTEGER)
                .constraints(
                        constraint("pk_rpkit_locked_block").primaryKey(RPKIT_LOCKED_BLOCK.ID)
                )
                .execute()
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.1.0")
        }
    }

    override fun insert(entity: RPKLockedBlock): Int {
        database.create
                .insertInto(
                        RPKIT_LOCKED_BLOCK,
                        RPKIT_LOCKED_BLOCK.WORLD,
                        RPKIT_LOCKED_BLOCK.X,
                        RPKIT_LOCKED_BLOCK.Y,
                        RPKIT_LOCKED_BLOCK.Z
                )
                .values(
                        entity.block.world.name,
                        entity.block.x,
                        entity.block.y,
                        entity.block.z
                )
                .execute()
        val id = database.create.lastID().toInt()
        entity.id = id
        return id
    }

    override fun update(entity: RPKLockedBlock) {
        database.create
                .update(RPKIT_LOCKED_BLOCK)
                .set(RPKIT_LOCKED_BLOCK.WORLD, entity.block.world.name)
                .set(RPKIT_LOCKED_BLOCK.X, entity.block.x)
                .set(RPKIT_LOCKED_BLOCK.Y, entity.block.y)
                .set(RPKIT_LOCKED_BLOCK.Z, entity.block.z)
                .where(RPKIT_LOCKED_BLOCK.ID.eq(entity.id))
                .execute()
    }

    override fun get(id: Int): RPKLockedBlock? {
        val result = database.create
                .select(
                        RPKIT_LOCKED_BLOCK.WORLD,
                        RPKIT_LOCKED_BLOCK.X,
                        RPKIT_LOCKED_BLOCK.Y,
                        RPKIT_LOCKED_BLOCK.Z
                )
                .from(RPKIT_LOCKED_BLOCK)
                .where(RPKIT_LOCKED_BLOCK.ID.eq(id))
                .fetchOne() ?: return null
        val lockedBlock = RPKLockedBlock(
                id,
                plugin.server.getWorld(result.get(RPKIT_LOCKED_BLOCK.WORLD)).getBlockAt(
                        result.get(RPKIT_LOCKED_BLOCK.X),
                        result.get(RPKIT_LOCKED_BLOCK.Y),
                        result.get(RPKIT_LOCKED_BLOCK.Z)
                )
        )
        return lockedBlock
    }

    fun get(block: Block): RPKLockedBlock? {
        val result = database.create
                .select(RPKIT_LOCKED_BLOCK.ID)
                .from(RPKIT_LOCKED_BLOCK)
                .where(RPKIT_LOCKED_BLOCK.WORLD.eq(block.world.name))
                .and(RPKIT_LOCKED_BLOCK.X.eq(block.x))
                .and(RPKIT_LOCKED_BLOCK.Y.eq(block.y))
                .and(RPKIT_LOCKED_BLOCK.Z.eq(block.z))
                .fetchOne() ?: return null
        return get(result.get(RPKIT_LOCKED_BLOCK.ID))
    }

    override fun delete(entity: RPKLockedBlock) {
        database.create
                .deleteFrom(RPKIT_LOCKED_BLOCK)
                .where(RPKIT_LOCKED_BLOCK.ID.eq(entity.id))
                .execute()
    }
}