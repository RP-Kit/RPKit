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

package com.seventh_root.elysium.chat.bukkit.exception

class ChatChannelMessagePostProcessingException: ChatChannelMessageProcessingException {

    constructor() {
    }

    constructor(s: String): super(s) {
    }

    constructor(s: String, throwable: Throwable): super(s, throwable) {
    }

    constructor(throwable: Throwable): super(throwable) {
    }

    constructor(s: String, throwable: Throwable, b: Boolean, b1: Boolean): super(s, throwable, b, b1) {
    }

}
