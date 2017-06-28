/*
 * This file is generated by jOOQ.
*/
package com.rpkit.chat.bukkit.database.jooq.rpkit.tables.records;


import com.rpkit.chat.bukkit.database.jooq.rpkit.tables.ChatGroupMember;

import javax.annotation.Generated;

import org.jooq.Field;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Row3;
import org.jooq.impl.UpdatableRecordImpl;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.9.2"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class ChatGroupMemberRecord extends UpdatableRecordImpl<ChatGroupMemberRecord> implements Record3<Integer, Integer, Integer> {

    private static final long serialVersionUID = -495525657;

    /**
     * Setter for <code>rpkit.chat_group_member.id</code>.
     */
    public void setId(Integer value) {
        set(0, value);
    }

    /**
     * Getter for <code>rpkit.chat_group_member.id</code>.
     */
    public Integer getId() {
        return (Integer) get(0);
    }

    /**
     * Setter for <code>rpkit.chat_group_member.chat_group_id</code>.
     */
    public void setChatGroupId(Integer value) {
        set(1, value);
    }

    /**
     * Getter for <code>rpkit.chat_group_member.chat_group_id</code>.
     */
    public Integer getChatGroupId() {
        return (Integer) get(1);
    }

    /**
     * Setter for <code>rpkit.chat_group_member.minecraft_profile_id</code>.
     */
    public void setMinecraftProfileId(Integer value) {
        set(2, value);
    }

    /**
     * Getter for <code>rpkit.chat_group_member.minecraft_profile_id</code>.
     */
    public Integer getMinecraftProfileId() {
        return (Integer) get(2);
    }

    // -------------------------------------------------------------------------
    // Primary key information
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Record1<Integer> key() {
        return (Record1) super.key();
    }

    // -------------------------------------------------------------------------
    // Record3 type implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Integer, Integer, Integer> fieldsRow() {
        return (Row3) super.fieldsRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Row3<Integer, Integer, Integer> valuesRow() {
        return (Row3) super.valuesRow();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field1() {
        return ChatGroupMember.CHAT_GROUP_MEMBER.ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field2() {
        return ChatGroupMember.CHAT_GROUP_MEMBER.CHAT_GROUP_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Field<Integer> field3() {
        return ChatGroupMember.CHAT_GROUP_MEMBER.MINECRAFT_PROFILE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value1() {
        return getId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value2() {
        return getChatGroupId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer value3() {
        return getMinecraftProfileId();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatGroupMemberRecord value1(Integer value) {
        setId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatGroupMemberRecord value2(Integer value) {
        setChatGroupId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatGroupMemberRecord value3(Integer value) {
        setMinecraftProfileId(value);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ChatGroupMemberRecord values(Integer value1, Integer value2, Integer value3) {
        value1(value1);
        value2(value2);
        value3(value3);
        return this;
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    /**
     * Create a detached ChatGroupMemberRecord
     */
    public ChatGroupMemberRecord() {
        super(ChatGroupMember.CHAT_GROUP_MEMBER);
    }

    /**
     * Create a detached, initialised ChatGroupMemberRecord
     */
    public ChatGroupMemberRecord(Integer id, Integer chatGroupId, Integer minecraftProfileId) {
        super(ChatGroupMember.CHAT_GROUP_MEMBER);

        set(0, id);
        set(1, chatGroupId);
        set(2, minecraftProfileId);
    }
}
