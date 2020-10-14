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

CREATE TABLE `rpkit_discord_profile`
(
    `id`         int        NOT NULL AUTO_INCREMENT,
    `profile_id` int            NULL,
    `discord_id` bigint(20) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_github_profile`
(
    `id`          int           NOT NULL AUTO_INCREMENT,
    `profile_id`  int               NULL,
    `name`        varchar(256)  NOT NULL,
    `oauth_token` varchar(1024) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_irc_profile`
(
    `id`         int          NOT NULL AUTO_INCREMENT,
    `profile_id` int              NULL,
    `nick`       varchar(256) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_minecraft_profile`
(
    `id`             int         NOT NULL AUTO_INCREMENT,
    `profile_id`     int             NULL,
    `minecraft_uuid` varchar(36) NOT NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `rpkit_minecraft_profile_link_request`
(
    `profile_id`           int NOT NULL,
    `minecraft_profile_id` int NOT NULL
);

CREATE TABLE `rpkit_profile`
(
    `id`            int         NOT NULL AUTO_INCREMENT,
    `name`          varchar(16) NOT NULL,
    `password_hash` blob,
    `password_salt` blob,
    PRIMARY KEY (`id`)
);