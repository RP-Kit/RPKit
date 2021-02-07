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

CREATE TABLE `rpkit_auction`
(
    `id`                    integer primary key     NOT NULL,
    `item`                  blob    NOT NULL,
    `currency_name`         varchar(256)     NOT NULL,
    `world`                 varchar(256) DEFAULT NULL,
    `x`                     double       DEFAULT NULL,
    `y`                     double       DEFAULT NULL,
    `z`                     double       DEFAULT NULL,
    `yaw`                   real         DEFAULT NULL,
    `pitch`                 real         DEFAULT NULL,
    `character_id`          int     NOT NULL,
    `duration`              bigint  NOT NULL,
    `end_time`              bigint  NOT NULL,
    `start_price`           int     NOT NULL,
    `buy_out_price`         int          DEFAULT NULL,
    `no_sell_price`         int          DEFAULT NULL,
    `minimum_bid_increment` int     NOT NULL,
    `bidding_open`          boolean NOT NULL
);

CREATE TABLE `rpkit_bid`
(
    `id`           integer primary key NOT NULL,
    `auction_id`   int NOT NULL,
    `character_id` int NOT NULL,
    `amount`       int NOT NULL
);