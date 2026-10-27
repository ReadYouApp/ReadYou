package me.ash.reader.domain.model.account.security

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
class LocalSecurityKey() : SecurityKey() {

    constructor(value: String?) : this() {
        decode<LocalSecurityKey>(value)
    }

    override fun toString(): String =
        DESUtils.encrypt(json.encodeToString(this))
}
