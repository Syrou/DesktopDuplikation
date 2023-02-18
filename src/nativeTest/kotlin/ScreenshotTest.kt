import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CArrayPointer
import kotlin.test.Test
import kotlin.test.assertTrue

class ScreenshotTest {

    @Test
    fun testScreenShot(){
        val resource = Resource("test.bmp")
        val desktopDuplikationManager = DesktopDuplikationManager()
        desktopDuplikationManager.use {
            if(!desktopDuplikationManager.initialize()) return
            desktopDuplikationManager.captureNext { sr, desc ->
                desktopDuplikationManager.dumpBitmap(
                    resource.path(),
                    sr.pData as CArrayPointer<ByteVar>,
                    sr.RowPitch.toInt(),
                    desc.Width.toInt(),
                    desc.Height.toInt()
                )
            }
        }

        assertTrue(resource.fileSize() > 0)
        assertTrue(resource.exists())
        assertTrue(resource.delete())
    }
}