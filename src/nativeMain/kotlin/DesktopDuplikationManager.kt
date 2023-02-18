import directx.*
import directx.CloseDesktop
import directx.HRESULT
import directx.MessageBoxA
import directx.OpenInputDesktop
import directx.SetThreadDesktop
import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.*

class DesktopDuplikationManager : AutoCloseable {
    val arena = Arena()
    private val D3DDeviceContext = D3D11DeviceContext()
    private val D3DDevice = D3D11Device()
    private val DeskDupl = DXGIOutputDuplication()
    private val OutputDesc = arena.alloc<DXGI_OUTPUT_DESC>()
    var haveFrameLock = false

    fun initialize(): Boolean {
        val hDesk = OpenInputDesktop(0, 0, GENERIC_ALL) ?: return false
        val outputNumber = 0
        val deskAttached: Boolean = SetThreadDesktop(hDesk) != 0
        CloseDesktop(hDesk);
        if (!deskAttached) {
            println("Failed to attach recording thread to desktop")
            return false
        }

        // Initialize DirectX
        var hr: HRESULT = S_OK
        OutputDesc.Rotation = 2u
        hr = D3DDevice.createDevice(D3DDeviceContext)
        if(FAILED(hr)){
            MessageBoxA(null, "D3DDevice->createDevice failed", null, 0)
            return false
        }

        // Get DXGI device
        val dxgiDevice = DXGIDevice()
        hr = D3DDevice.queryInterface(IID_IDXGIDevice, dxgiDevice.ptr.reinterpret())
        if (FAILED(hr)) {
            MessageBoxA(null, "D3DDevice->QueryInterface failed", null, 0)
            return false
        }

        // Get DXGI adapter
        val dxgiAdapter = DXGIAdapter()
        hr = dxgiDevice.GetParent(IID_IDXGIAdapter, dxgiAdapter.ptr)
        if (FAILED(hr)) {
            MessageBoxA(null, "dxgiDevice->GetParent failed", null, 0)
            return false
        }

        dxgiDevice.Release()

        // Get output
        val dxgiOutput = DXGIOutput()
        hr = dxgiAdapter.enumOutputs(outputNumber = outputNumber, dxgiOutput = dxgiOutput.ptr.ptr.reinterpret())
        if (FAILED(hr)) {
            MessageBoxA(null, "dxgiOutput->EnumOutputs failed", null, 0)
            return false
        }

        dxgiAdapter.Release()
        dxgiOutput.getDesc(OutputDesc)

        // QI for Output 1
        val dxgiOutput1 = DXGIOutput1()
        hr = dxgiOutput.queryInterface(IID_IDXGIOutput1, dxgiOutput1.ptr.ptr.reinterpret())
        if (FAILED(hr)) {
            MessageBoxA(null, "dxgiOutput->QueryInterface failed", null, 0)
            return false
        }

        dxgiOutput.Release()

        hr = dxgiOutput1.duplicateOutput(d3D11Device = D3DDevice, outputDuplication = DeskDupl.ptr.reinterpret())
        if (FAILED(hr)) {
            if (hr == DXGI_ERROR_NOT_CURRENTLY_AVAILABLE) {
                MessageBoxA(null, "Too many desktop recorders already active!", null, 0)
            }
            MessageBoxA(null, "DuplicateOutput failed", null, 0)
            return false
        }
        return true
    }

    fun captureNext(format:DXGI_FORMAT = DXGI_FORMAT_B8G8R8A8_UNORM, data: (D3D11_MAPPED_SUBRESOURCE, D3D11_TEXTURE2D_DESC) -> Unit) = memScoped {
        var hr: HRESULT
        if (haveFrameLock) {
            haveFrameLock = false
            hr = DeskDupl.ReleaseFrame()
        }
        val desktopResource = DXGIResource()
        val frameInfo = alloc<DXGI_OUTDUPL_FRAME_INFO>()
        run breaking@{
            while (true) {
                hr = DeskDupl.AcquireNextFrame(INFINITE.toInt(), frameInfo, desktopResource.ptr.reinterpret())
                if (SUCEEDED(hr) && frameInfo.LastPresentTime.QuadPart != 0L) {
                    return@breaking
                }
                DeskDupl.ReleaseFrame()
            }
        }

        haveFrameLock = true

        val gpuTexture = D3D11Texture2D()
        hr = desktopResource.queryInterface(IID_ID3D11Texture2D, gpuTexture)
        if (FAILED(hr)) {
            MessageBoxA(null, "deskRes->QueryInterface failed", null, 0)
            return
        }
        desktopResource.Release()

        if (frameInfo.TotalMetadataBufferSize <= 0u) {
            MessageBoxA(null, "Could not obtain frameInfo TotalMetadataBufferSize", null, 0)
            return
        }

        val desc: D3D11_TEXTURE2D_DESC = alloc()
        gpuTexture.getDesc(desc)

        desc.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE or D3D11_CPU_ACCESS_READ
        desc.Usage = D3D11_USAGE_STAGING
        desc.Format = format
        desc.BindFlags = 0u
        desc.MiscFlags = 0u
        val cpuTex = D3D11Texture2D()
        hr = D3DDevice.createTexture2D(desc, null, cpuTex)
        if (SUCEEDED(hr)) {
            D3DDeviceContext.CopyResource(cpuTex, gpuTexture)
        } else {
            MessageBoxA(null, "D3DDevice->CreateTexture2D failed", null, 0)
            return
        }

        val sr: D3D11_MAPPED_SUBRESOURCE = alloc()
        hr = D3DDeviceContext.Map(cpuTex, 0, D3D11_MAP_READ, 0, sr)
        if (SUCEEDED(hr)) {
            data.invoke(sr, desc)
            D3DDeviceContext.Unmap(cpuTex, 0)
        } else {
            MessageBoxA(null, "D3DDeviceContext->Map failed", null, 0)
        }

        cpuTex.Release()
        gpuTexture.Release()
    }

    fun dumpBitmap(filename: String, imageBuffer: CArrayPointer<ByteVar>, stride: Int, width: Int, height: Int) =
        memScoped {
            val header: BITMAPFILEHEADER = alloc()
            val info: BITMAPINFO = alloc()
            val imageSize = width * height * 3

            header.bfType = ('M'.code.shl(8) or 'B'.code).toUShort()
            header.bfSize = sizeOf<BITMAPFILEHEADER>().toUInt() + sizeOf<BITMAPINFO>().toUInt() + imageSize.toUInt()
            header.bfOffBits = sizeOf<BITMAPFILEHEADER>().toUInt() + sizeOf<BITMAPINFO>().toUInt()

            info.bmiHeader.biSize = sizeOf<BITMAPINFOHEADER>().toUInt()
            info.bmiHeader.biWidth = width
            info.bmiHeader.biHeight = if (height < 0) height else -height
            info.bmiHeader.biPlanes = 1u
            info.bmiHeader.biBitCount = 24u
            info.bmiHeader.biCompression = BI_RGB.toUInt()
            info.bmiHeader.biSizeImage = imageSize.toUInt()
            val file: CPointerVar<FILE> = alloc()
            val result: errno_t = fopen_s(file.ptr, filename, "wb")
            if (result == 0) {
                fwrite(header.ptr, 1, sizeOf<BITMAPFILEHEADER>().toULong(), file.value)
                fwrite(info.ptr, 1, sizeOf<BITMAPINFO>().toULong(), file.value)
                for (i in 0 until height) {
                    for (k in 0 until width) {
                        val pixelOffset: Int = i * stride + k * 4
                        val r = allocArray<ByteVar>(1)
                        r[0] = imageBuffer[pixelOffset + 2]
                        val g = allocArray<ByteVar>(1)
                        g[0] = imageBuffer[pixelOffset + 1]
                        val b = allocArray<ByteVar>(1)
                        b[0] = imageBuffer[pixelOffset]
                        fwrite(b, 1, 1, file.value)
                        fwrite(g, 1, 1, file.value)
                        fwrite(r, 1, 1, file.value)
                    }
                }
                val closeRes = fclose(file.value)
                if (closeRes == 0) {
                    println("Should have written!")
                } else {
                    println("File could not close?")
                }
            } else {
                println("Failed opening file with error: ${strerror(result)}")
            }
        }

    /**
     * clear() - Clears all allocated memory. Either use @see AutoClosable#use
     */
    override fun close() {
        DeskDupl.Release()
        D3DDeviceContext.Release()
        D3DDevice.release()
        haveFrameLock = false
        arena.clear()
        println("Everything cleared!")
    }
}