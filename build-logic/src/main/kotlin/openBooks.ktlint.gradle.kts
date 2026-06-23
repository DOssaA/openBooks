import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register
import org.jlleitschuh.gradle.ktlint.KtlintExtension

plugins {
    id("org.jlleitschuh.gradle.ktlint")
}

val ktlintExcludes = listOf(
    "**/build/**",
    "**/.gradle/**",
    "**/.idea/**",
    "**/kotlin-js-store/**"
)

fun Project.configureKtlint() {
    extensions.configure<KtlintExtension> {
        filter {
            ktlintExcludes.forEach(::exclude)
        }
    }

    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(tasks.named("ktlintCheck"))
    }
}

configureKtlint()

val ktlintAll = tasks.register("ktlintAll") {
    group = "verification"
    description = "Runs ktlint checks across root build scripts and all Kotlin projects."
    dependsOn(tasks.named("ktlintCheck"))
}

subprojects {
    var ktlintConfigured = false

    fun configureProjectKtlint() {
        if (ktlintConfigured) {
            return
        }

        ktlintConfigured = true
        pluginManager.apply("org.jlleitschuh.gradle.ktlint")
        configureKtlint()

        rootProject.tasks.named("ktlintAll").configure {
            dependsOn(tasks.named("ktlintCheck"))
        }
    }

    listOf(
        "org.jetbrains.kotlin.multiplatform",
        "org.jetbrains.kotlin.android",
        "com.android.application",
        "com.android.library",
        "com.android.kotlin.multiplatform.library"
    ).forEach { pluginId ->
        pluginManager.withPlugin(pluginId) {
            configureProjectKtlint()
        }
    }
}

tasks.named("check").configure {
    dependsOn(ktlintAll)
}
