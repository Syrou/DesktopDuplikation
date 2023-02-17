import directx.HRESULT
import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking
//import platform.windows.HRESULT


fun FAILED(hr: HRESULT): Boolean {
    return hr < 0
}

fun SUCEEDED(hr: HRESULT): Boolean {
    return hr >= 0
}

fun screenShot() {
    val desktopDuplicationManager = DesktopDuplicationManager()
    desktopDuplicationManager.initialize()
    desktopDuplicationManager.captureNext { sr, desc ->
        println("CAPTURED SCREEN?")
        desktopDuplicationManager.dumpRGBAtoRGBBmp("c:\\test.bmp", sr.pData as CArrayPointer<ByteVar>, sr.RowPitch.toInt(), desc.Width.toInt(), desc.Height.toInt())

    }
    desktopDuplicationManager.clear()
}

fun main() {
    runBlocking {
        screenShot()
    }

}