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

CREATE TABLE `rpkit_ticket`
(
    `id`          int           NOT NULL AUTO_INCREMENT,
    `reason`      varchar(1024) NOT NULL,
    `issuer_id`   int           NOT NULL,
    `resolver_id` int                    DEFAULT NULL,
    `world`       varchar(256)           DEFAULT NULL,
    `x`           double                 DEFAULT NULL,
    `y`           double                 DEFAULT NULL,
    `z`           double                 DEFAULT NULL,
    `yaw`         real                   DEFAULT NULL,
    `pitch`       real                   DEFAULT NULL,
    `open_date`   timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `close_date`  timestamp     NULL     DEFAULT NULL,
    `closed`      boolean       NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_vanished`
(
    `minecraft_profile_id` int NOT NULL,
    PRIMARY KEY (`minecraft_profile_id`)
);

CREATE TABLE `rpkit_warning`
(
    `id`         int           NOT NULL AUTO_INCREMENT,
    `reason`     varchar(1024) NOT NULL,
    `profile_id` int           NOT NULL,
    `issuer_id`  int           NOT NULL,
    `time`       timestamp     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);