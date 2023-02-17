import directx.HRESULT
import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking

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