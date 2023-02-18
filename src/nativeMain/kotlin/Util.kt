import directx.HRESULT

fun FAILED(hr: HRESULT): Boolean {
    return hr < 0
}

fun SUCEEDED(hr: HRESULT): Boolean {
    return hr >= 0
}

inline fun <T : AutoCloseable?, R> T.use(block: (T) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch (e: Exception) {
        closed = true
        try {
            this?.close()
        } catch (closeException: Exception) {
        }
        throw e
    } finally {
        if (!closed) {
            this?.close()
        }
    }
}