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

CREATE TABLE `rpkit_character_profession`
(
    `character_id` int          NOT NULL,
    `profession`   varchar(256) NOT NULL,
    PRIMARY KEY (`character_id`)
);

CREATE TABLE `rpkit_character_profession_change_cooldown`
(
    `character_id`      int       NOT NULL,
    `cooldown_end_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`character_id`)
);

CREATE TABLE `rpkit_character_profession_experience`
(
    `character_id` int          NOT NULL,
    `profession`   varchar(256) NOT NULL,
    `experience`   int          NOT NULL
);

CREATE TABLE `rpkit_profession_hidden`
(
    `character_id` int NOT NULL,
    PRIMARY KEY (`character_id`)
);
