import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm("desktop")

    sourceSets {
        val desktopMain by getting

        commonMain.dependencies {
            dependencies {
                implementation("com.twelvemonkeys.imageio:imageio-webp:3.12.0")
                implementation("com.twelvemonkeys.imageio:imageio-tiff:3.12.0")
                implementation("com.twelvemonkeys.imageio:imageio-psd:3.12.0")

                implementation("io.github.vinceglb:filekit-dialogs-compose:0.10.0-beta04")
                implementation("com.github.skydoves:landscapist-coil3:2.5.1")

                implementation("org.bytedeco:javacv:1.5.12")
                implementation("org.bytedeco:ffmpeg:7.1.1-1.5.12")
                implementation("org.bytedeco:ffmpeg-platform:7.1.1-1.5.12")

                implementation("com.mayakapps.compose:window-styler:0.3.3-SNAPSHOT")
                implementation("org.jetbrains.compose.material3:material3-desktop:1.9.0-alpha02")
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.ui)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.runtimeCompose)
            }
            commonTest.dependencies {
                implementation(libs.kotlin.test)
            }
            desktopMain.dependencies {
                implementation(compose.desktop.currentOs)
                implementation(libs.kotlinx.coroutinesSwing)
            }
        }
    }
}


compose.desktop {
    application {
        mainClass = "org.example.project.MainKt"
        args += listOf("-customArgument")

        jvmArgs("-Dcompose.jbr=true")
        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Deb, TargetFormat.Dmg, TargetFormat.Msi)
            packageName = "VideoImageConverter"
            packageVersion = "1.5.0"
            windows {
                packageVersion = "1.5.0"
                exePackageVersion = "1.5.0"
                msiPackageVersion = "1.5.0"
                iconFile.set(project.file("src/desktopMain/composeResources/drawable/icon.ico"))
            }
            linux {
                packageVersion = "1.5.0"
                debPackageVersion = "1.5.0"
                iconFile.set(project.file("src/desktopMain/composeResources/drawable/icons.png"))

            }



        }

        buildTypes.release.proguard {
            isEnabled.set(false)
            optimize.set(true)
            obfuscate.set(true)
            joinOutputJars.set(true)

        }
    }

}
