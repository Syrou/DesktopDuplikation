import directx.*
import kotlinx.cinterop.*

class DXGIDevice {
    private val arena:Arena = Arena()
    val ptr: CPointerVar<IDXGIDevice> = arena.alloc()
    val vtable:IDXGIDeviceVtbl? by lazy { ptr.value?.pointed?.lpVtbl?.pointed }

        /*val dxgiDeviceVtable = dxgiDevice.value?.pointed?.lpVtbl?.pointed
    hr = dxgiDeviceVtable?.GetParent?.invoke(
        dxgiDevice.ptr.pointed.value,
        IID_IDXGIAdapter.ptr,
        dxgiAdapter.ptr.reinterpret()
    ) ?: -1*/
    fun GetParent(guid: _GUID, dxgiAdapter:CPointerVar<IDXGIAdapter>):HRESULT{
        return vtable?.GetParent?.invoke(
            ptr.ptr.pointed.value,
            guid.ptr,
            dxgiAdapter.ptr.reinterpret()
        ) ?: -1
    }

    fun Release(){
        vtable?.Release?.invoke(ptr.value)
        arena.clear()
    }
}