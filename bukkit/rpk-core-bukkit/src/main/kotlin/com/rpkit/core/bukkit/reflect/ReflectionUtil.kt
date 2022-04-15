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

package com.rpkit.core.bukkit.reflect

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method


object ReflectionUtil {
    /*
     * The server version string to location NMS & OBC classes
     */
    private var versionString: String? = null

    /*
     * Cache of NMS classes that we've searched for
     */
    private val loadedNMSClasses: MutableMap<String, Class<*>?> = HashMap()

    /*
     * Cache of OBS classes that we've searched for
     */
    private val loadedOBCClasses: MutableMap<String, Class<*>?> = HashMap()

    /*
     * Cache of methods that we've found in particular classes
     */
    private val loadedMethods: MutableMap<Class<*>, MutableMap<String, Method?>> = HashMap()

    /*
     * Cache of fields that we've found in particular classes
     */
    private val loadedFields: MutableMap<Class<*>, MutableMap<String, Field?>> = HashMap()

    /**
     * Gets the version string for NMS & OBC class paths
     *
     * @return The version string of OBC and NMS packages
     */
    val version: String?
        get() {
            if (versionString == null) {
                val name = Bukkit.getServer().javaClass.getPackage().name
                versionString = name.substring(name.lastIndexOf('.') + 1) + "."
            }
            return versionString
        }

    /**
     * Get an NMS Class
     *
     * @param nmsClassName The name of the class
     * @return The class
     */
    fun getNMSClass(nmsClassName: String): Class<*>? {
        if (loadedNMSClasses.containsKey(nmsClassName)) {
            return loadedNMSClasses[nmsClassName]
        }
        val clazzName = "net.minecraft.$nmsClassName"
        val clazz = try {
            Class.forName(clazzName)
        } catch (t: Throwable) {
            t.printStackTrace()
            return loadedNMSClasses.put(nmsClassName, null)
        }
        loadedNMSClasses[nmsClassName] = clazz
        return clazz
    }

    /**
     * Get a class from the org.bukkit.craftbukkit package
     *
     * @param obcClassName the path to the class
     * @return the found class at the specified path
     */
    @Synchronized
    fun getOBCClass(obcClassName: String): Class<*>? {
        if (loadedOBCClasses.containsKey(obcClassName)) {
            return loadedOBCClasses[obcClassName]
        }
        val clazzName = "org.bukkit.craftbukkit.$version$obcClassName"
        val clazz: Class<*>
        try {
            clazz = Class.forName(clazzName)
        } catch (t: Throwable) {
            t.printStackTrace()
            loadedOBCClasses[obcClassName] = null
            return null
        }
        loadedOBCClasses[obcClassName] = clazz
        return clazz
    }

    /**
     * Get a Bukkit [Player] players NMS playerConnection object
     *
     * @param player The player
     * @return The players connection
     */
    fun getConnection(player: Player): Any? {
        val getHandleMethod = getMethod(player.javaClass, "getHandle")
        if (getHandleMethod != null) {
            try {
                val nmsPlayer = getHandleMethod.invoke(player)
                val playerConField = getField(nmsPlayer.javaClass, "playerConnection")
                return playerConField!![nmsPlayer]
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * Get a classes constructor
     *
     * @param clazz  The constructor class
     * @param params The parameters in the constructor
     * @return The constructor object
     */
    fun getConstructor(clazz: Class<*>, vararg params: Class<*>?): Constructor<*>? {
        return try {
            clazz.getConstructor(*params)
        } catch (e: NoSuchMethodException) {
            null
        }
    }

    /**
     * Get a method from a class that has the specific paramaters
     *
     * @param clazz      The class we are searching
     * @param methodName The name of the method
     * @param params     Any parameters that the method has
     * @return The method with appropriate paramaters
     */
    fun getMethod(clazz: Class<*>, methodName: String, vararg params: Class<*>?): Method? {
        if (!loadedMethods.containsKey(clazz)) {
            loadedMethods[clazz] = HashMap()
        }
        val methods = loadedMethods[clazz]!!
        return if (methods.containsKey(methodName)) {
            methods[methodName]
        } else try {
            val method = clazz.getMethod(methodName, *params)
            methods[methodName] = method
            loadedMethods[clazz] = methods
            method
        } catch (e: Exception) {
            e.printStackTrace()
            methods[methodName] = null
            loadedMethods[clazz] = methods
            null
        }
    }

    /**
     * Get a field with a particular name from a class
     *
     * @param clazz     The class
     * @param fieldName The name of the field
     * @return The field object
     */
    fun getField(clazz: Class<*>, fieldName: String): Field? {
        if (!loadedFields.containsKey(clazz)) {
            loadedFields[clazz] = HashMap()
        }
        val fields = loadedFields[clazz]!!
        return if (fields.containsKey(fieldName)) {
            fields[fieldName]
        } else try {
            val field = clazz.getField(fieldName)
            fields[fieldName] = field
            loadedFields[clazz] = fields
            field
        } catch (e: Exception) {
            e.printStackTrace()
            fields[fieldName] = null
            loadedFields[clazz] = fields
            null
        }
    }
}