plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    base
    id("openBooks.detekt")
    id("openBooks.ktlint")
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidMultiplatformLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}