package me.ash.reader.domain.model.account.security

class FeverSecurityKey private constructor() : SecurityKey() {

    var serverUrl: String? = null
    var username: String? = null
    var password: String? = null
    var clientCertificateAlias: String? = null

    constructor(serverUrl: String?, username: String?, password: String?, clientCertificateAlias: String?) : this() {
        this.serverUrl = serverUrl
        this.username = username
        this.password = password
        this.clientCertificateAlias = clientCertificateAlias
    }

    constructor(value: String? = DESUtils.empty) : this() {
        decode(value, FeverSecurityKey::class.java).let {
            serverUrl = it.serverUrl
            username = it.username
            password = it.password
            clientCertificateAlias = it.clientCertificateAlias
        }
    }
}
