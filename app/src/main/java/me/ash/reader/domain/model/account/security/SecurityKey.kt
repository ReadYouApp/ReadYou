package me.ash.reader.domain.model.account.security

import kotlinx.serialization.json.Json

abstract class SecurityKey {

    @PublishedApi
    internal val json: Json = Json { ignoreUnknownKeys = true }

    inline fun <reified T> decode(value: String?): T =
        json.decodeFromString<T>(DESUtils.decrypt(value?.ifEmpty { DESUtils.empty } ?: DESUtils.empty))

    override fun equals(other: Any?): Boolean {
        return this.toString() == other.toString()
    }

    override fun hashCode(): Int {
        return javaClass.hashCode()
    }
}
