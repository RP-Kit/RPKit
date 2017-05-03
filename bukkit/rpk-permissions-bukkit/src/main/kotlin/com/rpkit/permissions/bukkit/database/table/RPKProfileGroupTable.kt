package com.rpkit.permissions.bukkit.database.table

import com.rpkit.core.database.Database
import com.rpkit.core.database.Table
import com.rpkit.core.database.use
import com.rpkit.permissions.bukkit.RPKPermissionsBukkit
import com.rpkit.permissions.bukkit.group.RPKGroupProvider
import com.rpkit.permissions.bukkit.group.RPKProfileGroup
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.ehcache.config.builders.CacheConfigurationBuilder
import org.ehcache.config.builders.CacheManagerBuilder
import org.ehcache.config.builders.ResourcePoolsBuilder
import java.sql.PreparedStatement
import java.sql.Statement.RETURN_GENERATED_KEYS


class RPKProfileGroupTable(database: Database, private val plugin: RPKPermissionsBukkit): Table<RPKProfileGroup>(database, RPKProfileGroup::class) {

    private val cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true)
    private val cache = cacheManager.createCache("cache",
            CacheConfigurationBuilder.newCacheConfigurationBuilder(Int::class.javaObjectType, RPKProfileGroup::class.java,
                    ResourcePoolsBuilder.heap(20L)))

    override fun create() {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS rpkit_profile_group(" +
                            "id INTEGER PRIMARY KEY AUTO_INCREMENT," +
                            "profile_id INTEGER," +
                            "group_name VARCHAR(256)" +
                    ")"
            ).use(PreparedStatement::executeUpdate)
        }
    }

    override fun applyMigrations() {
        if (database.getTableVersion(this) == null) {
            database.setTableVersion(this, "1.3.0")
        }
    }

    override fun insert(entity: RPKProfileGroup): Int {
        var id = 0
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "INSERT INTO rpkit_profile_group(profile_id, group_name) VALUES(?, ?)",
                    RETURN_GENERATED_KEYS
            ).use { statement ->
                statement.setInt(1, entity.profile.id)
                statement.setString(2, entity.group.name)
                statement.executeUpdate()
                val generatedKeys = statement.generatedKeys
                if (generatedKeys.next()) {
                    id = generatedKeys.getInt(1)
                    entity.id = id
                    cache.put(id, entity)
                }
            }
        }
        return id
    }

    override fun update(entity: RPKProfileGroup) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "UPDATE rpkit_profile_group SET profile_id = ?, group_name = ? WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.profile.id)
                statement.setString(2, entity.group.name)
                statement.setInt(3, entity.id)
                statement.executeUpdate()
                cache.put(entity.id, entity)
            }
        }
    }

    override fun get(id: Int): RPKProfileGroup? {
        if (cache.containsKey(id)) {
            return cache.get(id)
        } else {
            var profileGroup: RPKProfileGroup? = null
            database.createConnection().use { connection ->
                connection.prepareStatement(
                        "SELECT id, profile_id, group_name FROM rpkit_profile_group WHERE id = ?"
                ).use { statement ->
                    statement.setInt(1, id)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
                        val profile = profileProvider.getProfile(resultSet.getInt("profile_id"))
                        val groupProvider = plugin.core.serviceManager.getServiceProvider(RPKGroupProvider::class)
                        val group = groupProvider.getGroup(resultSet.getString("group_name"))
                        if (profile != null && group != null) {
                            profileGroup = RPKProfileGroup(
                                    resultSet.getInt("id"),
                                    profile,
                                    group
                            )
                            cache.put(id, profileGroup)
                        } else {
                            connection.prepareStatement(
                                    "DELETE FROM rpkit_profile_group WHERE id = ?"
                            ).use { statement ->
                                statement.setInt(1, resultSet.getInt("id"))
                                statement.executeUpdate()
                                cache.remove(resultSet.getInt("id"))
                            }
                        }
                    }
                }
            }
            return profileGroup
        }
    }

    fun get(profile: RPKProfile): List<RPKProfileGroup> {
        val profileGroups = mutableListOf<RPKProfileGroup>()
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "SELECT id FROM rpkit_profile_group WHERE profile_id = ?"
            ).use { statement ->
                statement.setInt(1, profile.id)
                val resultSet = statement.executeQuery()
                while (resultSet.next()) {
                    val profileGroup = get(resultSet.getInt("id"))
                    if (profileGroup != null) {
                        profileGroups.add(profileGroup)
                    }
                }
            }
        }
        return profileGroups
    }

    override fun delete(entity: RPKProfileGroup) {
        database.createConnection().use { connection ->
            connection.prepareStatement(
                    "DELETE FROM rpkit_profile_group WHERE id = ?"
            ).use { statement ->
                statement.setInt(1, entity.id)
                statement.executeUpdate()
                cache.remove(entity.id)
            }
        }
    }

}