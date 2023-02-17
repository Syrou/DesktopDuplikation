import directx.*
import kotlinx.cinterop.*
import platform.windows.HRESULT

class DXGIResource {
    private val arena: Arena = Arena()
    val ptr: CPointerVar<IDXGIResource> = arena.alloc()
    val vtable: IDXGIResourceVtbl? by lazy { ptr.value?.pointed?.lpVtbl?.pointed }

    fun queryInterface(guid: _GUID, device: D3D11Texture2D):HRESULT{
        return  ptr.value?.pointed?.lpVtbl?.pointed?.QueryInterface?.invoke(ptr.ptr.pointed.value, guid.ptr, device.ptr.ptr.reinterpret()) ?: -1
    }

    fun Release(){
        vtable?.Release?.invoke(ptr.value)
        arena.clear()
    }
}