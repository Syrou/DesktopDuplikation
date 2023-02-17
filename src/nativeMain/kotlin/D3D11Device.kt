import directx.*
import kotlinx.cinterop.*
import platform.windows.HRESULT
import platform.windows.S_OK

class D3D11Device {
    private val arena: Arena = Arena()
    val ptr: CPointerVar<ID3D11Device> = arena.alloc()
    private var vtable:ID3D11DeviceVtbl? = null

    fun createDevice(d3DDeviceContext: D3D11DeviceContext):HRESULT{
        var hr: HRESULT = S_OK
        val driverTypes = arrayOf(
            D3D_DRIVER_TYPE_HARDWARE,
            D3D_DRIVER_TYPE_WARP,
            D3D_DRIVER_TYPE_REFERENCE
        )

        val featureLevels = arrayOf(
            D3D_FEATURE_LEVEL_11_0,
            D3D_FEATURE_LEVEL_10_1,
            D3D_FEATURE_LEVEL_10_0,
            D3D_FEATURE_LEVEL_9_1
        )

        val numFeatureLevels = featureLevels.size
        val featureLevel = cValue<D3D_FEATURE_LEVELVar>()
        run breaking@{
            driverTypes.forEach {
                hr = D3D11CreateDevice(
                    null, it, null, 0, featureLevels.toUIntArray().toCValues(), numFeatureLevels.toUInt(),
                    D3D11_SDK_VERSION, ptr.ptr, featureLevel, d3DDeviceContext.ptr.ptr
                )
                if (SUCEEDED(hr))
                    return@breaking
            }
        }

        return if (FAILED(hr)) {
            MessageBoxA(null, "D3D11CreateDevice failed", null, 0)
            -1
        } else {
            vtable = ptr.value?.pointed?.lpVtbl?.pointed
            0
        }
    }


    //val vtable = D3DDevice.ptr.value?.pointed?.lpVtbl?.pointed
    //hr = vtable?.QueryInterface?.invoke(D3DDevice.ptr.ptr.pointed.value, IID_IDXGIDevice.ptr, dxgiDevice.ptr.reinterpret()) ?: -1
    fun queryInterface(guid: _GUID, device: CPointer<COpaquePointerVar>):HRESULT{
        return  ptr.value?.pointed?.lpVtbl?.pointed?.QueryInterface?.invoke(ptr.ptr.pointed.value, guid.ptr, device) ?: -1
    }

    fun createTexture2D(desc:D3D11_TEXTURE2D_DESC, subresourceData: D3D11_SUBRESOURCE_DATA?, texture:D3D11Texture2D):HRESULT{
        return vtable?.CreateTexture2D?.invoke(ptr.value, desc.ptr, null, texture.ptr.ptr.reinterpret()) ?: -1
    }

    fun release(){
        ptr.value?.pointed?.lpVtbl?.pointed?.Release?.invoke(ptr.value)
        arena.clear()
    }
}