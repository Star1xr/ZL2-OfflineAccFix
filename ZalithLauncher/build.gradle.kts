import com.android.build.api.variant.FilterConfiguration.FilterType.ABI
import com.android.build.gradle.tasks.MergeSourceSetFolders
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    id("com.google.devtools.ksp")
    id("kotlinx-serialization")
    id("kotlin-parcelize")
    id("stringfog")
}
apply(plugin = "stringfog")

val zalithPackageName = "com.movtery.zalithlauncher"
val launcherAPPName = project.findProperty("launcher_app_name") as? String ?: error("The \"launcher_app_name\" property is not set in gradle.properties.")
val launcherName = project.findProperty("launcher_name") as? String ?: error("The \"launcher_name\" property is not set in gradle.properties.")
val launcherShortName = project.findProperty("launcher_short_name") as? String ?: error("The \"launcher_short_name\" property is not set in gradle.properties.")
val launcherUrl = project.findProperty("url_home") as? String ?: error("The \"url_home\" property is not set in gradle.properties.")

val launcherVersionCode = (project.findProperty("launcher_version_code") as? String)?.toIntOrNull() ?: error("The \"launcher_version_code\" property is not set as an integer in gradle.properties.")
val launcherVersionName = project.findProperty("launcher_version_name") as? String ?: error("The \"launcher_version_name\" property is not set in gradle.properties.")

val defaultOAuthClientID = project.findProperty("oauth_client_id") as? String
val defaultStorePassword = project.findProperty("default_store_password") as? String ?: error("The \"default_store_password\" property is not set in gradle.properties.")
val defaultKeyPassword = project.findProperty("default_key_password") as? String ?: error("The \"default_key_password\" property is not set in gradle.properties.")
val defaultCurseForgeApiKey = project.findProperty("curseforge_api_key") as? String

val generatedZalithDir = file("$buildDir/generated/source/zalith/java")

fun getKeyFromLocal(envKey: String, fileName: String? = null, default: String? = null): String {
    val key = System.getenv(envKey)
    return key ?: fileName?.let {
        val file = File(rootDir, fileName)
        if (file.canRead() && file.isFile) file.readText() else null
    } ?: default ?: run {
        logger.warn("BUILD: $envKey not set; related features may throw exceptions.")
        ""
    }
}

configure<com.github.megatronking.stringfog.plugin.StringFogExtension> {
    implementation = "com.github.megatronking.stringfog.xor.StringFogImpl"
    fogPackages = arrayOf("$zalithPackageName.info")
    kg = com.github.megatronking.stringfog.plugin.kg.RandomKeyGenerator()
    mode = com.github.megatronking.stringfog.plugin.StringFogMode.bytes
}

android {
    namespace = zalithPackageName
    compileSdk = 36

    signingConfigs {
        create("releaseBuild") {
            storeFile = file("zalith_launcher.jks")
            storePassword = getKeyFromLocal("STORE_PASSWORD", ".store_password.txt")
            keyAlias = "my-alias"
            keyPassword = getKeyFromLocal("KEY_PASSWORD", ".key_password.txt")
        }
        create("debugBuild") {
            storeFile = file("zalith_launcher_debug.jks")
            storePassword = defaultStorePassword
            keyAlias = "movtery_zalith_debug"
            keyPassword = defaultKeyPassword
        }
    }

    defaultConfig {
        applicationId = zalithPackageName
        applicationIdSuffix = ".v2"
        minSdk = 26
        targetSdk = 35
        versionCode = launcherVersionCode
        versionName = launcherVersionName
        manifestPlaceholders["launcher_name"] = launcherAPPName
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("releaseBuild")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            signingConfig = signingConfigs.getByName("debugBuild")
        }
    }

    sourceSets["main"].java.srcDirs(generatedZalithDir)

    androidComponents {
        onVariants { variant ->
            variant.outputs.forEach { output ->
                if (output is com.android.build.api.variant.impl.VariantOutputImpl) {
                    val variantName = variant.name.replaceFirstChar { it.uppercaseChar() }
                    afterEvaluate {
                        val task = tasks.named("merge${variantName}Assets").get() as MergeSourceSetFolders
                        task.doLast {
                            val arch = System.getProperty("arch", "all")
                            val assetsDir = task.outputDir.get().asFile
                            val jreList = listOf("jre-8", "jre-17", "jre-21")
                            println("arch:$arch")
                            jreList.forEach { jreVersion ->
                                val runtimeDir = File("$assetsDir/runtimes/$jreVersion")
                                println("runtimeDir:${runtimeDir.absolutePath}")
                                runtimeDir.listFiles()?.forEach {
                                    if (arch != "all" && it.name != "version" && !it.name.contains("universal") && it.name != "bin-${arch}.tar.xz") {
                                        println("delete:${it} : ${it.delete()}")
                                    }
                                }
                            }
                        }
                    }

                    (output.getFilter(ABI)?.identifier ?: "all").let { abi ->
                        val baseName = "$launcherName-${if (variant.buildType == "release") defaultConfig.versionName else "Debug-${defaultConfig.versionName}"}"
                        output.outputFileName = if (abi == "all") "$baseName.apk" else "$baseName-$abi.apk"
                    }
                }
            }
        }
    }

    splits {
        val arch = System.getProperty("arch", "all").takeIf { it != "all" } ?: return@splits
        abi {
            isEnable = true
            reset()
            when (arch) {
                "arm" -> include("armeabi-v7a")
                "arm64" -> include("arm64-v8a")
                "x86" -> include("x86")
                "x86_64" -> include("x86_64")
            }
        }
    }

    ndkVersion = "25.2.9519653"

    externalNativeBuild {
        ndkBuild {
            path = file("src/main/jni/Android.mk")
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
            pickFirsts += listOf("**/libbytehook.so")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        compose = true
        buildConfig = true
        prefab = true
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

fun generateJavaClass(
    sourceOutputDir: File,
    packageName: String,
    className: String,
    constantList: List<String>
) {
    val outputDir = File(sourceOutputDir, packageName.replace(".", "/"))
    outputDir.mkdirs()
    val javaFile = File(outputDir, "$className.java")
    javaFile.writeText(
        """
        |/**
        | * Automatically generated file. DO NOT MODIFY
        | */
        |package $packageName;
        |
        |public class $className {
        |${constantList.joinToString("\n") { "\t$it" }}
        |}
        """.trimMargin()
    )
    println("Generated Java file: ${javaFile.absolutePath}")
}

tasks.register("generateInfoDistributor") {
    doLast {
        fun String.toStatement(type: String = "String", variable: String) = "public static final $type $variable = $this;"

        val constantList = listOf(
            "\"${getKeyFromLocal("OAUTH_CLIENT_ID", ".oauth_client_id.txt", defaultOAuthClientID)}\"".toStatement(variable = "OAUTH_CLIENT_ID"),
            "\"$launcherAPPName\"".toStatement(variable = "LAUNCHER_NAME"),
            "\"$launcherName\"".toStatement(variable = "LAUNCHER_IDENTIFIER"),
            "\"$launcherShortName\"".toStatement(variable = "LAUNCHER_SHORT_NAME"),
            "\"$launcherUrl\"".toStatement(variable = "URL_HOME"),
            "\"${getKeyFromLocal("CURSEFORGE_API_KEY", ".curseforge_api.txt", defaultCurseForgeApiKey)}\"".toStatement(variable = "CURSEFORGE_API")
        )
        generateJavaClass(generatedZalithDir, "$zalithPackageName.info", "InfoDistributor", constantList)
    }
}

tasks.named("preBuild") {
    dependsOn("generateInfoDistributor")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.nav3)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.constraintlayout.compose)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.webkit)
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.network.ktor3)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.material)
    implementation(libs.material.color.utilities)
    implementation(libs.reorderable)
    implementation(libs.multiplatform.markdown.renderer)
    implementation(libs.multiplatform.markdown.renderer.m3)
    implementation(libs.multiplatform.markdown.renderer.coil3)
    implementation(libs.multiplatform.markdown.renderer.android)
    //Project
    implementation(project(":LayerController"))
    implementation(project(":ColorPicker"))
    implementation(project(":Terracotta"))
    //Utils
    implementation(libs.bytehook)
    implementation(libs.gson)
    implementation(libs.commons.io)
    implementation(libs.commons.codec)
    implementation(libs.commons.compress)
    implementation(libs.xz)
    implementation(libs.okio)
    implementation(libs.okhttp)
    implementation(libs.ktor.http)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.minidns.hla)
    implementation(libs.toml4j)
    implementation(libs.maven.artifact)
    implementation(libs.mmkv)
    implementation(libs.fishnet)
    implementation(libs.process.phoenix)
    implementation(libs.lunarcalendar)
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar", "*.aar"))))
    //Safe
    implementation(libs.stringfog.xor)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.sqlcipher.android)
    ksp(libs.androidx.room.compiler)
    //Support
    implementation(libs.proxy.client.android)
    //Hilt
    implementation(libs.dagger.hilt.android)
    ksp(libs.dagger.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    //Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
}
