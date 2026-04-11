package me.ash.reader.domain.model.account.security

class FeedlySecurityKey private constructor() : SecurityKey() {

    var accessToken: String? = null
    var userId: String? = null

    constructor(accessToken: String?, userId: String?) : this() {
        this.accessToken = accessToken
        this.userId = userId
    }

    constructor(value: String? = DESUtils.empty) : this() {
        decode(value, FeedlySecurityKey::class.java).let {
            accessToken = it.accessToken
            userId = it.userId
        }
    }
}
