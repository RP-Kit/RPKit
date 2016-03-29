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
    fun <T : ServiceProvider> getServiceProvider(type: Class<T>): T {
        return providers[type] as T
    }

}
