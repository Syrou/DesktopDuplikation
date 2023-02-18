import kotlinx.cinterop.*
import platform.posix.*

const val RESOURCE_PATH = "./src/nativeTest/resources"
class Resource(private val fileName: String) {

    fun exists(): Boolean {
        return memScoped {
            val buffer:stat = alloc()
            stat(path(), buffer.ptr) == 0
        }
    }

    fun path():String = "$RESOURCE_PATH/$fileName"

    fun delete():Boolean{
        return remove(path()) == 0
    }

    fun readText(): String {
        val size = fileSize()
        val file: CPointer<FILE>? = fopen("$RESOURCE_PATH/$fileName", "r")
        return memScoped {
            val tmp = allocArray<ByteVar>(size)
            fread(tmp, sizeOf<ByteVar>().convert(), size.convert(), file)
            fclose(file)
            tmp.toKString()
        }
    }

    fun fileSize():Int{
        val fp = fopen("$RESOURCE_PATH/$fileName", "r");
        fseek(fp, 0, SEEK_END)
        val size = ftell(fp)
        rewind(fp)
        fclose(fp)
        return size
    }
}