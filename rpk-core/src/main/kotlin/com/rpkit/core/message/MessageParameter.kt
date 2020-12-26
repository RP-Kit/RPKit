package com.rpkit.core.message

data class MessageParameter(val key: String, val value: String)

infix fun String.to(value: String) = MessageParameter(this, value)