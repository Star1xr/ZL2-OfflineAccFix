// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp.plugin) apply false
    kotlin("plugin.serialization") version libs.versions.kotlin apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.hilt) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath(libs.stringfog.gradle.plugin)
        classpath(libs.stringfog.xor)
        classpath("com.android.tools:r8:9.0.22") 
    }
}
