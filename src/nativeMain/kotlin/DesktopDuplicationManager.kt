import directx.*
import directx.CloseDesktop
import directx.HRESULT
import directx.MessageBoxA
import directx.OpenInputDesktop
import directx.SetThreadDesktop
import kotlinx.cinterop.*
import platform.posix.*
import platform.windows.*

class DesktopDuplicationManager {
    val arena = Arena()
    val D3DDeviceContext = D3D11DeviceContext()
    val D3DDevice = D3D11Device()
    val DeskDupl = DXGIOutputDuplication()//arena.alloc<CPointerVar<IDXGIOutputDuplication>>()
    val OutputDesc = arena.alloc<DXGI_OUTPUT_DESC>()
    var haveFrameLock = false

    fun initialize():Boolean{
        val hDesk = OpenInputDesktop(0, 0, GENERIC_ALL) ?: return false
        val OutputNumber = 0
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

        // Get DXGI device
        val dxgiDevice = DXGIDevice()
         hr = D3DDevice.queryInterface(IID_IDXGIDevice, dxgiDevice.ptr.reinterpret())
        if (FAILED(hr)) {
            MessageBoxA(null, "D3DDevice->QueryInterface failed", null, 0)
            return false
        } else {
            println("D3DDevice->QueryInterface success!")
        }


        // Get DXGI adapter
        val dxgiAdapter = DXGIAdapter()
        hr = dxgiDevice.GetParent(IID_IDXGIAdapter, dxgiAdapter.ptr)
        if (FAILED(hr)) {
            MessageBoxA(null, "dxgiDevice->GetParent failed", null, 0)
            return false
        } else {
            println("dxgiDevice->GetParent success!")
        }
        dxgiDevice.Release()

        // Get output
        val dxgiOutput = DXGIOutput()
        hr = dxgiAdapter.enumOutputs(outputNumber = OutputNumber, dxgiOutput = dxgiOutput.ptr.ptr.reinterpret())
        if (FAILED(hr)) {
            MessageBoxA(null, "dxgiOutput->EnumOutputs failed", null, 0)
            return false
        } else {
            println("dxgiOutput->EnumOutputs success!")
        }
        dxgiAdapter.Release()
        dxgiOutput.getDesc(OutputDesc)

        // QI for Output 1
        val dxgiOutput1 = DXGIOutput1()
        hr = dxgiOutput.queryInterface(IID_IDXGIOutput1, dxgiOutput1.ptr.ptr.reinterpret())
        if (FAILED(hr)) {
            MessageBoxA(null, "dxgiOutput->QueryInterface failed", null, 0)
            return false
        } else {
            println("dxgiOutput->QueryInterface success!")
        }
        dxgiOutput.Release()

        hr = dxgiOutput1.duplicateOutput(d3D11Device = D3DDevice, outputDuplication = DeskDupl.ptr.reinterpret())
        if (FAILED(hr)) {
            if (hr == DXGI_ERROR_NOT_CURRENTLY_AVAILABLE) {
                MessageBoxA(null, "Too many desktop recorders already active!", null, 0)
            }
            MessageBoxA(null, "DuplicateOutput failed", null, 0)
            return false
        } else {
            println("DuplicateOutput success!")
        }
        return true
    }

    fun captureNext( data: (D3D11_MAPPED_SUBRESOURCE, D3D11_TEXTURE2D_DESC)->Unit) {
        memScoped {
            var hr:HRESULT
            if(haveFrameLock){
                haveFrameLock = false
                hr = DeskDupl.ReleaseFrame()
            }
            val desktopResource = DXGIResource()
            val frameInfo = alloc<DXGI_OUTDUPL_FRAME_INFO>()
            run breaking@{
                while(true){
                    println(1)
                    hr = DeskDupl.AcquireNextFrame(INFINITE.toInt(), frameInfo, desktopResource.ptr.reinterpret())
                    if(SUCEEDED(hr) && frameInfo.LastPresentTime.QuadPart != 0L){
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

            if(frameInfo.TotalMetadataBufferSize > 0u){
                println("We have buffer data!")
            }else{
                println("We do not have buffer data!")
            }
            var ok = true

            val desc:D3D11_TEXTURE2D_DESC = alloc()
            gpuTexture.getDesc(desc)

            desc.CPUAccessFlags = D3D11_CPU_ACCESS_WRITE or D3D11_CPU_ACCESS_READ
            desc.Usage = D3D11_USAGE_STAGING
            desc.Format = DXGI_FORMAT_B8G8R8A8_UNORM
            desc.BindFlags = 0u
            desc.MiscFlags = 0u
            val cpuTex = D3D11Texture2D()
            hr = D3DDevice.createTexture2D(desc, null, cpuTex)
            if (SUCEEDED(hr)) {
                D3DDeviceContext.CopyResource(cpuTex, gpuTexture)
            } else {
                ok = false
                MessageBoxA(null, "D3DDevice->CreateTexture2D failed", null, 0)
            }
            println("Before SR!?")
            //UINT                     subresource = D3D11CalcSubresource(0, 0, 0);
            val sr:D3D11_MAPPED_SUBRESOURCE = alloc()
            //var screenData:ScreenData? = null
            hr = D3DDeviceContext.Map(cpuTex, 0, D3D11_MAP_READ, 0, sr)
            if (SUCEEDED(hr)) {
                println("SR info rowpitch: ${sr.RowPitch} depthpitch: ${sr.DepthPitch} format: ${desc.Format}")
                println("DESC WIDTH: ${desc.Width} DESC HEIGHT: ${desc.Height}")
                /*dumpRGBAtoRGBBmp("c:\\test.bmp",
                    sr.pData as CArrayPointer<ByteVar>, sr.RowPitch.toInt(), desc.Width.toInt(), desc.Height.toInt())*/
                /*val screenData = ScreenData(desc.Width.toInt(), desc.Height.toInt(), null, sr.RowPitch.toInt())
                screenData.buffer = allocArray(desc.Width.toInt() * desc.Height.toInt() * 4){
                    0
                }
                for (y in 0..desc.Height.toInt()) {
                    memcpy(screenData.buffer + y * desc.Width.toInt() * 4, sr.pData as CPointer<uint8_tVar> + sr.RowPitch.toInt() * y,  desc.Width.toULong() * 4u)
                }*/
                data.invoke(sr, desc)
                //screenData.buffer = null
                D3DDeviceContext.Unmap(cpuTex, 0)
            } else {
                ok = false
                MessageBoxA(null, "D3DDeviceContext->Map failed", null, 0)
            }

            cpuTex.Release()
            gpuTexture.Release()
        }
    }

    fun dumpRGBAtoRGBBmp(filename:String, imageBuffer:CArrayPointer<ByteVar>, stride:Int, width: Int, height: Int) = memScoped {
        println("dumpRGBAtoRGBBmp")
        val header:BITMAPFILEHEADER = alloc()
        val info: BITMAPINFO = alloc()
        val imageSize = width * height * 3

        header.bfType = ('M'.code.shl(8) or 'B'.code).toUShort()
        header.bfSize = sizeOf<BITMAPFILEHEADER>().toUInt() + sizeOf<BITMAPINFO>().toUInt() + imageSize.toUInt()
        header.bfOffBits = sizeOf<BITMAPFILEHEADER>().toUInt() + sizeOf<BITMAPINFO>().toUInt()

        info.bmiHeader.biSize = sizeOf<BITMAPINFOHEADER>().toUInt()
        info.bmiHeader.biWidth = width
        info.bmiHeader.biHeight = if(height < 0 )height else -height
        info.bmiHeader.biPlanes = 1u
        info.bmiHeader.biBitCount = 24u
        info.bmiHeader.biCompression = BI_RGB.toUInt()
        info.bmiHeader.biSizeImage = imageSize.toUInt()
        println("SETUP BITMAP WITH WIDTH: $width HEIGHT: $height")
        val file:CPointerVar<FILE> = alloc()
        println("Trying to open file: $filename")
        val result:errno_t = fopen_s(file.ptr, filename, "wb")
        if(result == 0){
            fwrite(header.ptr, 1, sizeOf<BITMAPFILEHEADER>().toULong(), file.value)
            fwrite(info.ptr, 1, sizeOf<BITMAPINFO>().toULong(), file.value)
            for(i in 0 until height){
                for (k in 0 until width){
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
            if(closeRes == 0){
                println("Should have written!")
            }else{
                println("File could not close?")
            }

        }else{
            println("Failed opening file with error: ${strerror(result)}")
        }
    }

    fun clear(){
        DeskDupl.Release()
        D3DDeviceContext.Release()
        D3DDevice.release()
        haveFrameLock = false
        arena.clear()
    }
}