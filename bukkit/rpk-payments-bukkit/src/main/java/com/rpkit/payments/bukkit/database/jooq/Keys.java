/*
 * Copyright 2022 Ren Binden
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

/*
 * This file is generated by jOOQ.
 */
package com.rpkit.payments.bukkit.database.jooq;


import com.rpkit.payments.bukkit.database.jooq.tables.RpkitPaymentGroup;
import com.rpkit.payments.bukkit.database.jooq.tables.records.RpkitPaymentGroupRecord;

import org.jooq.TableField;
import org.jooq.UniqueKey;
import org.jooq.impl.DSL;
import org.jooq.impl.Internal;


/**
 * A class modelling foreign key relationships and constraints of tables in
 * rpkit_payments.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Keys {

    // -------------------------------------------------------------------------
    // UNIQUE and PRIMARY KEY definitions
    // -------------------------------------------------------------------------

    public static final UniqueKey<RpkitPaymentGroupRecord> KEY_RPKIT_PAYMENT_GROUP_PRIMARY = Internal.createUniqueKey(RpkitPaymentGroup.RPKIT_PAYMENT_GROUP, DSL.name("KEY_rpkit_payment_group_PRIMARY"), new TableField[] { RpkitPaymentGroup.RPKIT_PAYMENT_GROUP.ID }, true);
}
