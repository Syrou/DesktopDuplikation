import kotlinx.cinterop.*
import kotlinx.coroutines.runBlocking

fun screenShot() {
    val desktopDuplikationManager = DesktopDuplikationManager()
    desktopDuplikationManager.use {
        if(!desktopDuplikationManager.initialize()) return
        desktopDuplikationManager.captureNext { sr, desc ->
            desktopDuplikationManager.dumpBitmap(
                "c:\\test.bmp",
                sr.pData as CArrayPointer<ByteVar>,
                sr.RowPitch.toInt(),
                desc.Width.toInt(),
                desc.Height.toInt()
            )
        }
    }
}

fun main() {
    screenShot()
}