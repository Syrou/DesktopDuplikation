import directx.*
import kotlinx.cinterop.*
import platform.windows.HRESULT

class DXGIAdapter {
    private val arena: Arena = Arena()
    val ptr: CPointerVar<IDXGIAdapter> = arena.alloc()
    val vtable: IDXGIAdapterVtbl? by lazy { ptr.value?.pointed?.lpVtbl?.pointed }

    /*val dxgiAdapterVtable = dxgiAdapter.value?.pointed?.lpVtbl?.pointed
    hr = dxgiAdapterVtable?.EnumOutputs?.invoke(
    dxgiAdapter.ptr.pointed.value,
    OutputNumber.toUInt(),
    dxgiOutput.ptr.reinterpret()
    ) ?: -1*/
    fun enumOutputs(outputNumber:Int, dxgiOutput:CPointer<CPointerVar<IDXGIOutput>>):HRESULT{
        return vtable?.EnumOutputs?.invoke(
            ptr.ptr.pointed.value,
            outputNumber.toUInt(),
            dxgiOutput
        ) ?: -1
    }

    fun Release(){
        vtable?.Release?.invoke(ptr.value)
        arena.clear()
    }
}