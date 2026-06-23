import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.kotlin.dsl.register

plugins {
    id("io.gitlab.arturbosch.detekt")
}

val detektJvmTarget = "11"
val detektConfigFile = rootProject.file("config/detekt.yml")
val detektBaselineFile = rootProject.file("config/baseline.xml")
val detektExcludes = listOf(
    "**/build/**",
    "**/.gradle/**",
    "**/.idea/**",
    "**/kotlin-js-store/**"
)

fun Project.configureDetekt(includePatterns: List<String>) {
    extensions.configure<DetektExtension> {
        buildUponDefaultConfig = true
        allRules = false
        config.setFrom(detektConfigFile)

        if (detektBaselineFile.exists()) {
            baseline = detektBaselineFile
        }
    }

    tasks.named<Detekt>("detekt").configure {
        description = "Runs Detekt for ${project.path}."
        parallel = true
        setSource(files(projectDir))
        include(includePatterns)
        exclude(detektExcludes)
        jvmTarget = detektJvmTarget

        reports {
            html.required.set(true)
            sarif.required.set(true)
        }
    }

    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(tasks.named("detekt"))
    }
}

configureDetekt(
    includePatterns = listOf(
        "*.gradle.kts",
        "gradle/**/*.kts"
    )
)

val detektAll = tasks.register("detektAll") {
    group = "verification"
    description = "Runs Detekt across root build scripts and all Kotlin projects."
    dependsOn(tasks.named("detekt"))
}

subprojects {
    var detektConfigured = false

    fun configureProjectDetekt() {
        if (detektConfigured) {
            return
        }

        detektConfigured = true
        pluginManager.apply("io.gitlab.arturbosch.detekt")

        configureDetekt(
            includePatterns = listOf(
                "src/**/*.kt",
                "src/**/*.kts",
                "*.gradle.kts"
            )
        )

        rootProject.tasks.named("detektAll").configure {
            dependsOn(tasks.named("detekt"))
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
            configureProjectDetekt()
        }
    }
}

tasks.named("check").configure {
    dependsOn(detektAll)
}
