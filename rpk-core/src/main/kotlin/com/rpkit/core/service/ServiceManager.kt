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

package com.rpkit.core.service

import com.rpkit.core.exception.UnregisteredServiceException
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * Manages service providers.
 */
class ServiceManager {

    private val providers: MutableMap<KClass<out ServiceProvider>, ServiceProvider>

    init {
        providers = ConcurrentHashMap()
    }

    /**
     * Registers a service provider with this service manager.
     * The service provider will be registered for all interfaces it is assignable from.
     */
    fun registerServiceProvider(provider: ServiceProvider) {
        for (providerInterface in provider.javaClass.interfaces) {
            if (ServiceProvider::class.java.isAssignableFrom(providerInterface)) {
                providers[providerInterface.asSubclass(ServiceProvider::class.java).kotlin] = provider
            }
            providers[provider.javaClass.kotlin] = provider
        }
    }

    /**
     * Gets a service provider by type.
     * Uses a [Class] to allow easy usage from Java.
     * There is an alternative method available which uses a [KClass] to make this easier from Kotlin.
     *
     * @param type The type of the service provider required.
     * @return The service provider
     * @throws UnregisteredServiceException If no service is found with the type.
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(UnregisteredServiceException::class)
    fun <T: ServiceProvider> getServiceProvider(type: Class<T>): T {
        return getServiceProvider(type.kotlin)
    }

    /**
     * Gets a service provider by type.
     * Uses a [KClass] to allow easy usage from Kotlin.
     * There is an alternative method available which uses a [Class] to make this easier from Java.
     *
     * @param type The type of the service provider required.
     * @return The service provider
     * @throws UnregisteredServiceException If no service is found with the type.
     */
    @Suppress("UNCHECKED_CAST")
    @Throws(UnregisteredServiceException::class)
    fun <T: ServiceProvider> getServiceProvider(type: KClass<T>): T {
        return providers[type] as? T ?: throw UnregisteredServiceException(type)
    }

}
