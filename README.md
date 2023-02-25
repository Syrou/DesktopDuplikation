# Desktop Duplikation
A library to make usage of Windows Desktop Duplication API easy
on Kotlin Native for target mingw64.


[![Kotlin](https://img.shields.io/badge/kotlin-1.8.10-blue.svg?logo=kotlin)](http://kotlinlang.org)

# Dependencies
In order to build this project you need to install directx 11 headers
to your msys2 installation. Either create a system environment variable
called MINGW64_DIR and point to your msys2 installation, or
install it under C:/msys64/mingw64

```shell 
$ pacman -S mingw-w64-x86_64-headers-git
```

If you wish to use this code as a pre-compiled library, put this into your gradle mingw64 dependencies:

```groovy
implementation("io.github.syrou:desktopduplikation:0.0.3")
```

# Usage
In order to automatically clear arena allocation, use the .use {} lambda
```kotlin
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
```
