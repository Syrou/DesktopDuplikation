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

```kotlin
    val desktopDuplicationManager = DesktopDuplicationManager()
    desktopDuplicationManager.initialize()
    desktopDuplicationManager.captureNext { sr, desc ->
        desktopDuplicationManager.dumpRGBAtoRGBBmp("c:\\test.bmp", sr.pData as CArrayPointer<ByteVar>, sr.RowPitch.toInt(), desc.Width.toInt(), desc.Height.toInt())
    }
    desktopDuplicationManager.clear()
```
