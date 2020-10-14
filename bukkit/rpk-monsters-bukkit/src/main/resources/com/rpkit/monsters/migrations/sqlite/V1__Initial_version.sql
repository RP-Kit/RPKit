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

CREATE TABLE `rpkit_monster_spawn_area`
(
    `id`    integer primary key          NOT NULL,
    `world` varchar(256) NOT NULL,
    `min_x` int          NOT NULL,
    `min_y` int          NOT NULL,
    `min_z` int          NOT NULL,
    `max_x` int          NOT NULL,
    `max_y` int          NOT NULL,
    `max_z` int          NOT NULL
);

CREATE TABLE `rpkit_monster_spawn_area_monster`
(
    `monster_spawn_area_id` int          NOT NULL,
    `entity_type`           varchar(256) NOT NULL,
    `min_level`             int          NOT NULL,
    `max_level`             int          NOT NULL
);