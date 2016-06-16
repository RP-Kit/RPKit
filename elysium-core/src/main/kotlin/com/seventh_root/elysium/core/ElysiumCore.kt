package com.seventh_root.elysium.core

import com.seventh_root.elysium.core.database.Database
import com.seventh_root.elysium.core.service.ServiceManager

import java.util.logging.Logger

class ElysiumCore(val logger: Logger, val database: Database) {

    val serviceManager: ServiceManager

    init {
        serviceManager = ServiceManager()
    }

}
