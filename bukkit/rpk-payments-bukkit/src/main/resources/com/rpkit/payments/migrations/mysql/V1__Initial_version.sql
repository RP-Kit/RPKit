/*
 * Copyright 2021 Ren Binden
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

CREATE TABLE `rpkit_payment_group`
(
    `id`                int          NOT NULL AUTO_INCREMENT,
    `name`              varchar(256) NOT NULL,
    `amount`            int          NOT NULL,
    `currency_name`     varchar(256)          DEFAULT NULL,
    `interval`          bigint(20)   NOT NULL,
    `last_payment_time` timestamp    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    `balance`           int          NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_payment_group_invite`
(
    `payment_group_id` int NOT NULL,
    `character_id`     int NOT NULL
);

CREATE TABLE `rpkit_payment_group_member`
(
    `payment_group_id` int NOT NULL,
    `character_id`     int NOT NULL
);

CREATE TABLE `rpkit_payment_group_owner`
(
    `payment_group_id` int NOT NULL,
    `character_id`     int NOT NULL
);

CREATE TABLE `rpkit_payment_notification`
(
    `id`           int           NOT NULL AUTO_INCREMENT,
    `group_id`     int           NOT NULL,
    `to_id`        int           NOT NULL,
    `character_id` int           NOT NULL,
    `date`         datetime      NOT NULL,
    `text`         varchar(1024) NOT NULL,
    PRIMARY KEY (`id`)
);