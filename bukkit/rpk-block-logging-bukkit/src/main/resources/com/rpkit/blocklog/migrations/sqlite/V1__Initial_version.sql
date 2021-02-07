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
    `id`                   integer primary key          NOT NULL,
    `block_history_id`     int          NOT NULL,
    `time`                 timestamp    NOT NULL,
    `profile_id`           int                   DEFAULT NULL,
    `minecraft_profile_id` int                   DEFAULT NULL,
    `character_id`         int                   DEFAULT NULL,
    `from`                 varchar(256) NOT NULL,
    `to`                   varchar(256) NOT NULL,
    `reason`               varchar(256) NOT NULL
);

CREATE TABLE `rpkit_block_history`
(
    `id`    integer primary key          NOT NULL,
    `world` varchar(256) NOT NULL,
    `x`     int          NOT NULL,
    `y`     int          NOT NULL,
    `z`     int          NOT NULL
);

CREATE TABLE `rpkit_block_inventory_change`
(
    `id`                   integer primary key          NOT NULL,
    `block_history_id`     int          NOT NULL,
    `time`                 timestamp    NOT NULL,
    `profile_id`           int                   DEFAULT NULL,
    `minecraft_profile_id` int                   DEFAULT NULL,
    `character_id`         int                   DEFAULT NULL,
    `from`                 blob         NOT NULL,
    `to`                   blob         NOT NULL,
    `reason`               varchar(256) NOT NULL
);