plugins {
    kotlin("multiplatform") version "1.8.10"
    id("convention.publication")
}

group = "io.github.syrou"
version = "0.0.2"

repositories {
    mavenCentral()
}

val mingwPath = File(System.getenv("MINGW64_DIR") ?: "C:/msys64/mingw64")

kotlin {
    mingwX64("native").apply {
        binaries {
            executable {
                entryPoint = "main"
                when (preset) {
                    presets["mingwX64"] -> linkerOpts("-L${mingwPath.resolve("lib")}")
                }
            }
        }
        compilations["main"].cinterops {
            val directx by creating {
                includeDirs {
                    allHeaders(
                        "C:\\msys64\\mingw64\\include"
                    )
                }
            }
        }
    }
    sourceSets {
        val nativeMain by getting{
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val nativeTest by getting
    }
}