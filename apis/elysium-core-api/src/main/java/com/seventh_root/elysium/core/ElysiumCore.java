package com.seventh_root.elysium.core;

import com.seventh_root.elysium.core.database.Database;
import com.seventh_root.elysium.core.service.ServiceManager;

import java.util.logging.Logger;

public class ElysiumCore {

    private final Logger logger;
    private final ServiceManager serviceManager;
    private final Database database;

    public ElysiumCore(Logger logger, Database database) {
        this.logger = logger;
        serviceManager = new ServiceManager();
        this.database = database;
    }

    public Logger getLogger() {
        return logger;
    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }

    public Database getDatabase() {
        return database;
    }

}
