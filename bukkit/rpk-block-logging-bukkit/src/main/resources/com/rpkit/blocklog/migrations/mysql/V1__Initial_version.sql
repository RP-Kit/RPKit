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

CREATE TABLE `rpkit_block_change`
(
    `id`                   int          NOT NULL AUTO_INCREMENT,
    `block_history_id`     int          NOT NULL,
    `time`                 timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `profile_id`           int                   DEFAULT NULL,
    `minecraft_profile_id` int                   DEFAULT NULL,
    `character_id`         int                   DEFAULT NULL,
    `from`                 varchar(256) NOT NULL,
    `to`                   varchar(256) NOT NULL,
    `reason`               varchar(256) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_block_history`
(
    `id`    int          NOT NULL AUTO_INCREMENT,
    `world` varchar(256) NOT NULL,
    `x`     int          NOT NULL,
    `y`     int          NOT NULL,
    `z`     int          NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_block_inventory_change`
(
    `id`                   int          NOT NULL AUTO_INCREMENT,
    `block_history_id`     int          NOT NULL,
    `time`                 timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `profile_id`           int                   DEFAULT NULL,
    `minecraft_profile_id` int                   DEFAULT NULL,
    `character_id`         int                   DEFAULT NULL,
    `from`                 blob         NOT NULL,
    `to`                   blob         NOT NULL,
    `reason`               varchar(256) NOT NULL,
    PRIMARY KEY (`id`)
);