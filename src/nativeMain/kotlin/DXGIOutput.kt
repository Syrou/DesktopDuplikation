import directx.*
import kotlinx.cinterop.*

class DXGIOutput {
    private val arena: Arena = Arena()
    val ptr: CPointerVar<IDXGIOutput> = arena.alloc()
    val vtable: IDXGIOutputVtbl? by lazy { ptr.value?.pointed?.lpVtbl?.pointed }

    /*val dxgiOutputVtable = dxgiOutput.value?.pointed?.lpVtbl?.pointed
    dxgiOutputVtable?.GetDesc?.invoke(dxgiOutput.value, OutputDesc.ptr)
    */
    fun getDesc(outputDesc: DXGI_OUTPUT_DESC):HRESULT{
        return vtable?.GetDesc?.invoke(
            ptr.value,
            outputDesc.ptr.reinterpret()
        ) ?: -1
    }

    fun queryInterface(guid: _GUID, device: CPointer<COpaquePointerVar>):HRESULT{
        return  ptr.value?.pointed?.lpVtbl?.pointed?.QueryInterface?.invoke(ptr.ptr.pointed.value, guid.ptr, device) ?: -1
    }

    fun Release(){
        vtable?.Release?.invoke(ptr.value)
        arena.clear()
    }
}