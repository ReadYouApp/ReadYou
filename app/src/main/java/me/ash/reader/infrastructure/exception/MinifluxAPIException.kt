package me.ash.reader.infrastructure.exception

class MinifluxAPIException : BusinessException {
    var statusCode: Int? = null
    var endpoint: String? = null

    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, statusCode: Int? = null, endpoint: String? = null) : super(message) {
        this.statusCode = statusCode
        this.endpoint = endpoint
    }
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(message: String, statusCode: Int? = null, endpoint: String? = null, cause: Throwable) : super(message, cause) {
        this.statusCode = statusCode
        this.endpoint = endpoint
    }
    constructor(cause: Throwable) : super(cause)

    override fun toString(): String {
        return "MinifluxAPIException(message='$message', statusCode=$statusCode, endpoint=$endpoint)"
    }
}
