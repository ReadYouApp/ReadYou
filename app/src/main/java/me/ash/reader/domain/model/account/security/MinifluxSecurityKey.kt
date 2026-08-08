package me.ash.reader.domain.model.account.security

class MinifluxSecurityKey private constructor() : SecurityKey() {

    var serverUrl: String? = null
    var apiToken: String? = null
    var username: String? = null
    var password: String? = null
    var clientCertificateAlias: String? = null

    constructor(
        serverUrl: String?,
        apiToken: String?,
        username: String?,
        password: String?,
        clientCertificateAlias: String?,
    ) : this() {
        this.serverUrl = serverUrl
        this.apiToken = apiToken
        this.username = username
        this.password = password
        this.clientCertificateAlias = clientCertificateAlias
    }

    constructor(value: String? = DESUtils.empty) : this() {
        decode(value, MinifluxSecurityKey::class.java).let {
            serverUrl = it.serverUrl
            apiToken = it.apiToken
            username = it.username
            password = it.password
            clientCertificateAlias = it.clientCertificateAlias
        }
    }
}
