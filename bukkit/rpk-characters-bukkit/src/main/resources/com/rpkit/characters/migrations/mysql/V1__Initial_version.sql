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

CREATE TABLE `rpkit_character`
(
    `id`                   int           NOT NULL AUTO_INCREMENT,
    `profile_id`           int          DEFAULT NULL,
    `minecraft_profile_id` int          DEFAULT NULL,
    `name`                 varchar(256)  NOT NULL,
    `gender`               varchar(256) DEFAULT NULL,
    `age`                  int           NOT NULL,
    `race_name`            varchar(256) DEFAULT NULL,
    `description`          varchar(2048) NOT NULL,
    `dead`                 boolean       NOT NULL,
    `world`                varchar(256)  NOT NULL,
    `x`                    double        NOT NULL,
    `y`                    double        NOT NULL,
    `z`                    double        NOT NULL,
    `yaw`                  real          NOT NULL,
    `pitch`                real          NOT NULL,
    `inventory_contents`   longblob      NOT NULL,
    `helmet`               blob,
    `chestplate`           blob,
    `leggings`             blob,
    `boots`                blob,
    `health`               double        NOT NULL,
    `max_health`           double        NOT NULL,
    `mana`                 int           NOT NULL,
    `max_mana`             int           NOT NULL,
    `food_level`           int           NOT NULL,
    `thirst_level`         int           NOT NULL,
    `profile_hidden`       boolean       NOT NULL,
    `name_hidden`          boolean       NOT NULL,
    `gender_hidden`        boolean       NOT NULL,
    `age_hidden`           boolean       NOT NULL,
    `race_hidden`          boolean       NOT NULL,
    `description_hidden`   boolean       NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_new_character_cooldown`
(
    `profile_id`         int      NOT NULL,
    `cooldown_timestamp` datetime NOT NULL,
    PRIMARY KEY (`profile_id`)
);