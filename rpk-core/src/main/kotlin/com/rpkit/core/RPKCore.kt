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

package com.rpkit.core

import com.rpkit.core.database.Database
import com.rpkit.core.service.ServiceManager
import com.rpkit.core.web.Web
import java.util.logging.Logger

/**
 * Represents the core of RPK.
 * Most core RPK functionality is accessible from here, including the logger, database, web and service managers.
 *
 * @property logger The logger to use
 * @property database The database instance to use
 * @property web The web instance to use
 */
class RPKCore(val logger: Logger, val database: Database, val web: Web) {

    /**
     * The service manager.
     * Manages service providers.
     */
    val serviceManager: ServiceManager

    init {
        serviceManager = ServiceManager()
        Thread {
            web.server.start()
            web.server.join()
        }.start()
    }

}
