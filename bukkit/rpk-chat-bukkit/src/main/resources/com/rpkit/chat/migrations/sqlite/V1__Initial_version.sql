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

CREATE TABLE `rpkit_chat_channel_mute`
(
    `minecraft_profile_id` int          NOT NULL,
    `chat_channel_name`    varchar(256) NOT NULL
);

CREATE TABLE `rpkit_chat_channel_speaker`
(
    `minecraft_profile_id` int          NOT NULL,
    `chat_channel_name`    varchar(256) NOT NULL
);

CREATE TABLE `rpkit_chat_group`
(
    `id`   integer primary key          NOT NULL,
    `name` varchar(256) NOT NULL
);

CREATE TABLE `rpkit_chat_group_invite`
(
    `chat_group_id`        int NOT NULL,
    `minecraft_profile_id` int NOT NULL
);

CREATE TABLE `rpkit_chat_group_member`
(
    `chat_group_id`        int NOT NULL,
    `minecraft_profile_id` int NOT NULL
);

CREATE TABLE `rpkit_last_used_chat_group`
(
    `minecraft_profile_id` int NOT NULL,
    `chat_group_id`        int NOT NULL
);

CREATE TABLE `rpkit_snooper`
(
    `minecraft_profile_id` int NOT NULL
);