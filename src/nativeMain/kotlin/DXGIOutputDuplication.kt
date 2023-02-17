import directx.*
import kotlinx.cinterop.*

class DXGIOutputDuplication {
    private val arena: Arena = Arena()
    val ptr: CPointerVar<IDXGIOutputDuplication> = arena.alloc()
    val vtable: IDXGIOutputDuplicationVtbl? by lazy { ptr.value?.pointed?.lpVtbl?.pointed }

    fun ReleaseFrame():HRESULT{
        return vtable?.ReleaseFrame?.invoke(ptr.value) ?: -1
    }

    fun AcquireNextFrame(timeout:Int, frameInfo: DXGI_OUTDUPL_FRAME_INFO,dxgiResource:CPointerVar<IDXGIResource>, ):HRESULT{
        return vtable?.AcquireNextFrame?.invoke(ptr.value, timeout.toUInt(), frameInfo.ptr, dxgiResource.ptr) ?: -1
    }

    fun Release(){
        vtable?.Release?.invoke(ptr.value)
        arena.clear()
    }
}