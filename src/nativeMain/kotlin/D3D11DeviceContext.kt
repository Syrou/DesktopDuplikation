import directx.*
import kotlinx.cinterop.*

class D3D11DeviceContext {
    private val arena: Arena = Arena()
    val ptr: CPointerVar<ID3D11DeviceContext> = arena.alloc()
    val vtable: ID3D11DeviceContextVtbl? by lazy { ptr.value?.pointed?.lpVtbl?.pointed }

    fun queryInterface(guid: _GUID, device: D3D11Texture2D):HRESULT{
        return  ptr.value?.pointed?.lpVtbl?.pointed?.QueryInterface?.invoke(ptr.ptr.pointed.value, guid.ptr, device.ptr.reinterpret()) ?: -1
    }

    fun CopyResource(destinationResource:D3D11Texture2D, sourceResource:D3D11Texture2D){
        vtable?.CopyResource?.invoke(ptr.value, destinationResource.ptr.value as CPointer<ID3D11Resource>, sourceResource.ptr.value as CPointer<ID3D11Resource>)
    }

    fun Map(resource:D3D11Texture2D, subresource:Int = 0, mapType:D3D11_MAP, mapFlags:Int=0, mappedSubresourceData: D3D11_MAPPED_SUBRESOURCE):HRESULT{
        return vtable?.Map?.invoke(ptr.value, resource.ptr.pointed?.reinterpret(),subresource.toUInt(), D3D11_MAP_READ, mapFlags.toUInt(), mappedSubresourceData.ptr.reinterpret()) ?: -1
    }

    fun Unmap(resource:D3D11Texture2D, subresource: Int){
        vtable?.Unmap?.invoke(ptr.value, resource.ptr.pointed?.reinterpret(), subresource.toUInt())
    }

    fun Release(){
        vtable?.Release?.invoke(ptr.value)
        arena.clear()
    }
}