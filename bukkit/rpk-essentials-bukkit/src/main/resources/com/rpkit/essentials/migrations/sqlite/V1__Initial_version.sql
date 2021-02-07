/*
 * Copyright 2020 Ren Binden
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

CREATE TABLE `rpkit_log_messages_enabled`
(
    `minecraft_profile_id` int NOT NULL,
    PRIMARY KEY (`minecraft_profile_id`)
);

CREATE TABLE `rpkit_previous_location`
(
    `minecraft_profile_id` int          NOT NULL,
    `world`                varchar(256) NOT NULL,
    `x`                    double       NOT NULL,
    `y`                    double       NOT NULL,
    `z`                    double       NOT NULL,
    `yaw`                  real         NOT NULL,
    `pitch`                real         NOT NULL,
    PRIMARY KEY (`minecraft_profile_id`)
);

CREATE TABLE `rpkit_tracking_disabled`
(
    `character_id` int NOT NULL,
    PRIMARY KEY (`character_id`)
);