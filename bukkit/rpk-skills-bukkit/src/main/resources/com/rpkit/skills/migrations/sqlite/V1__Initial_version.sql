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

CREATE TABLE `rpkit_skill_binding`
(
    `id`           integer primary key          NOT NULL,
    `character_id` int          NOT NULL,
    `item`         blob         NOT NULL,
    `skill_name`   varchar(256) NOT NULL
);

CREATE TABLE `rpkit_skill_cooldown`
(
    `character_id`       int          NOT NULL,
    `skill_name`         varchar(256) NOT NULL,
    `cooldown_timestamp` timestamp    NOT NULL
);
