import directx.*
import kotlinx.cinterop.*
import platform.windows.HRESULT

class DXGIOutput1 {
    private val arena: Arena = Arena()
    val ptr: CPointerVar<IDXGIOutput1> = arena.alloc()
    val vtable: IDXGIOutput1Vtbl? by lazy { ptr.value?.pointed?.lpVtbl?.pointed }

    /*val dxgiOutput1Vtable = dxgiOutput1.value?.pointed?.lpVtbl?.pointed
    hr = dxgiOutput1Vtable?.DuplicateOutput?.invoke(
    dxgiOutput1.ptr.pointed.value,
    D3DDevice.`this`.ptr.pointed.value?.reinterpret(),
    DeskDupl.ptr.reinterpret()
    ) ?: -1*/

    fun duplicateOutput(d3D11Device: D3D11Device, outputDuplication:CPointer<CPointerVar<IDXGIOutputDuplication>>):HRESULT{
        return vtable?.DuplicateOutput?.invoke(ptr.ptr.pointed.value, d3D11Device.ptr.ptr.pointed.value?.reinterpret(), outputDuplication) ?: -1
    }

    fun Release(){
        vtable?.Release?.invoke(ptr.value)
        arena.clear()
    }
}