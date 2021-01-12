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

/*
 * This file is generated by jOOQ.
 */
package com.rpkit.chat.bukkit.database.jooq.tables;


import com.rpkit.chat.bukkit.database.jooq.RpkitChat;
import com.rpkit.chat.bukkit.database.jooq.tables.records.RpkitChatChannelSpeakerRecord;

import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Name;
import org.jooq.Record;
import org.jooq.Row2;
import org.jooq.Schema;
import org.jooq.Table;
import org.jooq.TableField;
import org.jooq.TableOptions;
import org.jooq.impl.DSL;
import org.jooq.impl.TableImpl;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class RpkitChatChannelSpeaker extends TableImpl<RpkitChatChannelSpeakerRecord> {

    private static final long serialVersionUID = 1791460677;

    /**
     * The reference instance of <code>rpkit_chat.rpkit_chat_channel_speaker</code>
     */
    public static final RpkitChatChannelSpeaker RPKIT_CHAT_CHANNEL_SPEAKER = new RpkitChatChannelSpeaker();

    /**
     * The class holding records for this type
     */
    @Override
    public Class<RpkitChatChannelSpeakerRecord> getRecordType() {
        return RpkitChatChannelSpeakerRecord.class;
    }

    /**
     * The column <code>rpkit_chat.rpkit_chat_channel_speaker.minecraft_profile_id</code>.
     */
    public final TableField<RpkitChatChannelSpeakerRecord, Integer> MINECRAFT_PROFILE_ID = createField(DSL.name("minecraft_profile_id"), org.jooq.impl.SQLDataType.INTEGER.nullable(false), this, "");

    /**
     * The column <code>rpkit_chat.rpkit_chat_channel_speaker.chat_channel_name</code>.
     */
    public final TableField<RpkitChatChannelSpeakerRecord, String> CHAT_CHANNEL_NAME = createField(DSL.name("chat_channel_name"), org.jooq.impl.SQLDataType.VARCHAR(256).nullable(false), this, "");

    /**
     * Create a <code>rpkit_chat.rpkit_chat_channel_speaker</code> table reference
     */
    public RpkitChatChannelSpeaker() {
        this(DSL.name("rpkit_chat_channel_speaker"), null);
    }

    /**
     * Create an aliased <code>rpkit_chat.rpkit_chat_channel_speaker</code> table reference
     */
    public RpkitChatChannelSpeaker(String alias) {
        this(DSL.name(alias), RPKIT_CHAT_CHANNEL_SPEAKER);
    }

    /**
     * Create an aliased <code>rpkit_chat.rpkit_chat_channel_speaker</code> table reference
     */
    public RpkitChatChannelSpeaker(Name alias) {
        this(alias, RPKIT_CHAT_CHANNEL_SPEAKER);
    }

    private RpkitChatChannelSpeaker(Name alias, Table<RpkitChatChannelSpeakerRecord> aliased) {
        this(alias, aliased, null);
    }

    private RpkitChatChannelSpeaker(Name alias, Table<RpkitChatChannelSpeakerRecord> aliased, Field<?>[] parameters) {
        super(alias, null, aliased, parameters, DSL.comment(""), TableOptions.table());
    }

    public <O extends Record> RpkitChatChannelSpeaker(Table<O> child, ForeignKey<O, RpkitChatChannelSpeakerRecord> key) {
        super(child, key, RPKIT_CHAT_CHANNEL_SPEAKER);
    }

    @Override
    public Schema getSchema() {
        return RpkitChat.RPKIT_CHAT;
    }

    @Override
    public RpkitChatChannelSpeaker as(String alias) {
        return new RpkitChatChannelSpeaker(DSL.name(alias), this);
    }

    @Override
    public RpkitChatChannelSpeaker as(Name alias) {
        return new RpkitChatChannelSpeaker(alias, this);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitChatChannelSpeaker rename(String name) {
        return new RpkitChatChannelSpeaker(DSL.name(name), null);
    }

    /**
     * Rename this table
     */
    @Override
    public RpkitChatChannelSpeaker rename(Name name) {
        return new RpkitChatChannelSpeaker(name, null);
    }

    // -------------------------------------------------------------------------
    // Row2 type methods
    // -------------------------------------------------------------------------

    @Override
    public Row2<Integer, String> fieldsRow() {
        return (Row2) super.fieldsRow();
    }
}