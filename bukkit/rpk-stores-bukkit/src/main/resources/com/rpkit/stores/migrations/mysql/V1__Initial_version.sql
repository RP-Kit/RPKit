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

CREATE TABLE `rpkit_consumable_purchase`
(
    `id`             int NOT NULL AUTO_INCREMENT,
    `purchase_id`    int NOT NULL,
    `remaining_uses` int NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_consumable_store_item`
(
    `id`            int NOT NULL AUTO_INCREMENT,
    `store_item_id` int NOT NULL,
    `uses`          int NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_permanent_purchase`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `purchase_id` int NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_permanent_store_item`
(
    `id`            int NOT NULL AUTO_INCREMENT,
    `store_item_id` int NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_purchase`
(
    `id`            int       NOT NULL AUTO_INCREMENT,
    `store_item_id` int       NOT NULL,
    `profile_id`    int       NOT NULL,
    `purchase_date` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_store_item`
(
    `id`          int           NOT NULL AUTO_INCREMENT,
    `plugin`      varchar(128)  NOT NULL,
    `identifier`  varchar(128)  NOT NULL,
    `description` varchar(2048) NOT NULL,
    `cost`        int           NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_timed_purchase`
(
    `id`          int NOT NULL AUTO_INCREMENT,
    `purchase_id` int NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_timed_store_item`
(
    `id`            int    NOT NULL AUTO_INCREMENT,
    `store_item_id` int    NOT NULL,
    `duration`      bigint NOT NULL,
    PRIMARY KEY (`id`)
);