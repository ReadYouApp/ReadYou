package me.ash.reader.domain.model.account.security

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
class FreshRSSSecurityKey(
    var serverUrl: String? = null,
    var username: String? = null,
    var password: String? = null,
    var clientCertificateAlias: String? = null,
) : SecurityKey() {

    constructor(value: String?) : this() {
        decode<FreshRSSSecurityKey>(value).let {
            serverUrl = it.serverUrl
            username = it.username
            password = it.password
            clientCertificateAlias = it.clientCertificateAlias
        }
    }

    override fun toString(): String =
        DESUtils.encrypt(json.encodeToString(this))
}
