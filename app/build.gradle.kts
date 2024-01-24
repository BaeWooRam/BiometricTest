import org.jetbrains.dokka.base.DokkaBase
import org.jetbrains.dokka.base.DokkaBaseConfiguration
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.dokka")
}

android {
    namespace = "com.geekstudio.biometrictest"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.geekstudio.biometrictest"
        minSdk = 28
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

tasks.dokkaHtml.configure {
    outputDirectory.set(buildDir.resolve("documentation/html"))
}

tasks.dokkaGfm.configure {
    outputDirectory.set(File("../documentation/markdown"))
}

buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-base:1.9.10")
    }
}

tasks.withType<DokkaTask>().configureEach {
    moduleName.set(project.name)
    moduleVersion.set(project.version.toString())
    suppressObviousFunctions.set(true)
    suppressInheritedMembers.set(true)

    dokkaSourceSets {
        configureEach {
            skipDeprecated.set(true)
            noAndroidSdkLink.set(true)
        }
    }

    pluginConfiguration<DokkaBase, DokkaBaseConfiguration> {
        footerMessage = "2015 HYUNDAI HT Co., Ltd."
        separateInheritedMembers = false
        mergeImplicitExpectActualDeclarations = false
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.biometric:biometric:1.1.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}