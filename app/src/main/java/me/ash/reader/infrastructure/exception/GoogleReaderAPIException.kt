package me.ash.reader.infrastructure.exception

data class GoogleReaderAPIException(
    override val message: String,
    val query: String? = null,
    val params: List<Pair<String, String>>? = null,
    val form: List<Pair<String, String>>? = null,
) : BusinessException() {
    override fun toString(): String {
        return "GoogleReaderAPIException(message='$message', query=$query, params=$params, form=$form)"
    }
}
