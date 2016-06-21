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

package com.seventh_root.elysium.core.service

import java.util.concurrent.ConcurrentHashMap

class ServiceManager {

    private val providers: MutableMap<Class<out ServiceProvider>, ServiceProvider>

    init {
        providers = ConcurrentHashMap<Class<out ServiceProvider>, ServiceProvider>()
    }

    fun registerServiceProvider(provider: ServiceProvider) {
        for (providerInterface in provider.javaClass.interfaces) {
            if (ServiceProvider::class.java.isAssignableFrom(providerInterface)) {
                providers.put(providerInterface.asSubclass(ServiceProvider::class.java), provider)
            }
            providers.put(provider.javaClass, provider)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: ServiceProvider> getServiceProvider(type: Class<T>): T {
        return providers[type] as T
    }

}
