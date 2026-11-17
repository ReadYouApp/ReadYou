import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.aboutlibraries)
    alias(libs.plugins.room)
    alias(libs.plugins.hilt)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.parcelize)
}

fun fetchGitCommitHash(): String {
    val process =
        ProcessBuilder("git", "rev-parse", "--verify", "--short", "HEAD")
            .redirectErrorStream(true)
            .start()
    return process.inputStream.bufferedReader().use { it.readText().trim() }
}

val gitCommitHash = fetchGitCommitHash()
val keyProps = Properties()
val releaseKeyPropsFile: File = rootProject.file("signature/keystore_release.properties")
val debugKeyPropsFile: File = rootProject.file("signature/keystore.properties")


if (releaseKeyPropsFile.exists()) {
    println("Loading keystore properties from ${releaseKeyPropsFile.absolutePath}")
    keyProps.load(FileInputStream(releaseKeyPropsFile))
} else if (debugKeyPropsFile.exists()) {
    keyProps.load(FileInputStream(debugKeyPropsFile))
}

android {
    compileSdk { version = release(37) { minorApiLevel = 2 } }

    defaultConfig {
        applicationId = "me.ash.reader"
        minSdk = 26
        targetSdk = 34
        versionCode = 47
        versionName = "0.16.2"

        buildConfigField(
            "String",
            "USER_AGENT_STRING",
            "\"ReadYou/${versionName}(${versionCode})\"",
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        room { schemaDirectory("$projectDir/schemas") }

        ksp { arg("room.incremental", "true") }
    }

    flavorDimensions.add("channel")
    productFlavors {
        create("github") {
            isDefault = true
            dimension = "channel"
        }
        create("fdroid") { dimension = "channel" }
        create("googlePlay") {
            dimension = "channel"
            applicationIdSuffix = ".google.play"
        }
    }
    signingConfigs {
        create("release") {
            keyAlias = keyProps["keyAlias"] as String?
            keyPassword = keyProps["keyPassword"] as String?
            storeFile = keyProps["storeFile"]?.let { file(it as String) }
            storePassword = keyProps["storePassword"] as String?
        }
    }
    lint { disable.addAll(listOf("MissingTranslation", "ExtraTranslation")) }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
        all { signingConfig = signingConfigs.getByName("release") }
    }
    applicationVariants.all {
        outputs.all {
            (this as com.android.build.gradle.internal.api.BaseVariantOutputImpl).outputFileName =
                "ReadYou-${defaultConfig.versionName}-${gitCommitHash}.apk"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures { buildConfig = true }
    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
        resources.excludes.add("rome-utils-*.jar")
    }
    androidResources { generateLocaleConfig = true }
    namespace = "me.ash.reader"
}

kotlin {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

aboutLibraries { export { excludeFields.add("generated") } }

dependencies {
    // AboutLibraries
    implementation(libs.aboutlibraries.core)
    implementation(libs.aboutlibraries.compose)

    // Compose
    implementation(libs.compose.html)
    implementation(platform(libs.compose.bom.alpha))
    implementation(libs.androidx.ui.graphics)
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.util)
    implementation(libs.compose.material)
    implementation(libs.compose.material.icons.extended)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)

    // Coil
    implementation(libs.coil.base)
    implementation(libs.coil.compose)
    implementation(libs.coil.svg)
    implementation(libs.coil.gif)
    implementation(libs.coil.network.okhttp)
    implementation(libs.coil.network.cache.control)

    // Hilt
    implementation(libs.hilt.work)
    implementation(libs.hilt.android)
    debugImplementation(libs.compose.ui.tooling)
    ksp(libs.hilt.android.compiler)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.viewmodel)

    // AndroidX
    implementation(libs.android.svg)
    implementation(libs.opml.parser)
    implementation(libs.readability4j)
    implementation(libs.rome)
    implementation(libs.rome.modules)
    implementation(libs.telephoto)
    implementation(libs.okhttp)
    implementation(libs.okhttp.coroutines)
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.profileinstaller)
    implementation(libs.work.runtime.ktx)
    implementation(libs.datastore.preferences)
    implementation(libs.room.paging)
    implementation(libs.room.common)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    implementation(libs.paging.common.ktx)
    implementation(libs.paging.runtime.ktx)
    implementation(libs.paging.compose)
    implementation(libs.browser)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.appcompat)
    implementation(libs.glance.appwidget)
    implementation(libs.glance.appwidget.preview)
    implementation(libs.glance.material3)
    implementation(libs.glance.preview)

    implementation(libs.navigation3.runtime)
    implementation(libs.navigation3.ui)
    implementation(libs.lifecycle.viewmodel.navigation3)
    implementation(libs.navigationevent)
    implementation(libs.compose.material3.adaptive.navigation)
    implementation(libs.compose.material3.adaptive.layout)

    implementation(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.timber)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.junit.ext)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
}
