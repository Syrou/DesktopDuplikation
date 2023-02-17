import directx.*
import kotlinx.cinterop.*
import platform.windows.HRESULT

class D3D11Texture2D {
    private val arena: Arena = Arena()
    val ptr: CPointerVar<ID3D11Texture2D> = arena.alloc()
    val vtable: ID3D11Texture2DVtbl? by lazy { ptr.value?.pointed?.lpVtbl?.pointed }

    fun queryInterface(guid: _GUID, device: CPointer<COpaquePointerVar>):HRESULT{
        return  ptr.value?.pointed?.lpVtbl?.pointed?.QueryInterface?.invoke(ptr.ptr.pointed.value, guid.ptr, device) ?: -1
    }

    fun getDesc(outputDesc: D3D11_TEXTURE2D_DESC){
        vtable?.GetDesc?.invoke(
            ptr.value,
            outputDesc.ptr.reinterpret()
        )
    }

    fun Release(){
        vtable?.Release?.invoke(ptr.value)
        arena.clear()
    }
}