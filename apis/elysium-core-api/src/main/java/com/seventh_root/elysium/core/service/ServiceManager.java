package com.seventh_root.elysium.core.service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceManager {

    private final Map<Class<? extends ServiceProvider>, ServiceProvider> providers;

    public ServiceManager() {
        providers = new ConcurrentHashMap<>();
    }

    public void registerServiceProvider(ServiceProvider provider) {
        for (Class<?> providerInterface : provider.getClass().getInterfaces()) {
            if (ServiceProvider.class.isAssignableFrom(providerInterface)) {
                providers.put(providerInterface.asSubclass(ServiceProvider.class), provider);
            }
            providers.put(provider.getClass(), provider);
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends ServiceProvider> T getServiceProvider(Class<T> type) {
        return (T) providers.get(type);
    }

}
