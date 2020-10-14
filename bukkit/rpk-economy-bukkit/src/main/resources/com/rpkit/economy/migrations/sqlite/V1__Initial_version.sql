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

CREATE TABLE `rpkit_currency`
(
    `id`             integer primary key          NOT NULL,
    `name`           varchar(256) NOT NULL,
    `name_singular`  varchar(256) NOT NULL,
    `name_plural`    varchar(256) NOT NULL,
    `rate`           double       NOT NULL,
    `default_amount` int          NOT NULL,
    `material`       varchar(256) NOT NULL
);

CREATE TABLE `rpkit_money_hidden`
(
    `character_id` int NOT NULL,
    PRIMARY KEY (`character_id`)
);

CREATE TABLE `rpkit_wallet`
(
    `character_id` int NOT NULL,
    `currency_id`  int NOT NULL,
    `balance`      int NOT NULL,
    PRIMARY KEY (`character_id`, `currency_id`)
);
