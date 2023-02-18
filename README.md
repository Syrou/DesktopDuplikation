# Desktop Duplikation
A library to make usage of Windows Desktop Duplication API easy
on Kotlin Native for target mingw64.

# Dependencies
In order to build this project you need to install directx 11 headers
to your msys2 installation. Either create a system environment variable
called MINGW64_DIR and point to your msys2 installation, or
install it under C:/msys64/mingw64

```shell 
$ pacman -S mingw-w64-x86_64-headers-git
```

# Usage
In order to automatically clear arena allocation, use the .use {} lambda
```kotlin
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
```
