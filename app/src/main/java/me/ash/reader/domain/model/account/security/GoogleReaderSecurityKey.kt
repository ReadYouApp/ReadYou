package me.ash.reader.domain.model.account.security

class GoogleReaderSecurityKey private constructor() : SecurityKey() {

    var serverUrl: String? = null
    var username: String? = null
    var password: String? = null
    var clientCertificateAlias: String? = null
    var customHeaders: String? = null

    constructor(serverUrl: String?, username: String?, password: String?, clientCertificateAlias: String?, customHeaders: String? = null) : this() {
        this.serverUrl = serverUrl
        this.username = username
        this.password = password
        this.clientCertificateAlias = clientCertificateAlias
        this.customHeaders = customHeaders
    }

    constructor(value: String? = DESUtils.empty) : this() {
        decode(value, GoogleReaderSecurityKey::class.java).let {
            serverUrl = it.serverUrl
            username = it.username
            password = it.password
            clientCertificateAlias = it.clientCertificateAlias
            customHeaders = it.customHeaders
        }
    }
}