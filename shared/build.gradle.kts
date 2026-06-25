import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.mokkery)
    alias(libs.plugins.allOpen) // for tests only
    alias(libs.plugins.koin)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    compilerOptions {
        // Room generates an `actual object` for the @ConstructedBy database constructor.
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    android {
        namespace = "com.darioossa.openbooks.shared"
        compileSdk =
            libs.versions.android.compileSdk
                .get()
                .toInt()
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTest {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.ktor.client.okhttp)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.jetbrains.nav3.ui)
            implementation(libs.jetbrains.lifecycle.viewmodelNav3)
            implementation(libs.jetbrains.material3.adaptiveNav3)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.kotlinxJson)
            implementation(project.dependencies.platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.annotations)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewModel)
            implementation(libs.koin.compose.nav3)
            implementation(libs.room.runtime)
            implementation(libs.sqlite.bundled)
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotest)
            implementation(libs.turbine)
            implementation(libs.koin.test)
            implementation(libs.ktor.client.mock)
        }
        getByName("androidHostTest").dependencies {
            // Room's Android builder needs a Context; Robolectric provides one on the JVM host.
            implementation(libs.robolectric)
            implementation(libs.androidx.test.core)
            // Compose UI tests run on the JVM host via Robolectric (no device needed).
            implementation(libs.compose.uiTest)
            implementation(libs.compose.uiTestJunit4)
            implementation(libs.compose.uiTestManifest)
        }
        getByName("androidDeviceTest").dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.androidx.test.core)
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.testExt.junit)
        }
    }
}

val isTestingTask =
    gradle.startParameter.taskNames.any {
        it.contains("test", ignoreCase = true) || it.contains("connectedCheck", ignoreCase = true)
    }

if (isTestingTask) {
    allOpen {
        annotation("com.darioossa.openbooks.OpenForTest")
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
    ksp(libs.room.compiler)
}
