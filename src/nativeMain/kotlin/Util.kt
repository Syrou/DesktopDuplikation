import directx.HRESULT

fun FAILED(hr: HRESULT): Boolean {
    return hr < 0
}

fun SUCEEDED(hr: HRESULT): Boolean {
    return hr >= 0
}