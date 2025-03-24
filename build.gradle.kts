plugins {
    id("application")
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.serialization)
    alias(libs.plugins.shadowJar)
}

group = "com.ramitsuri"
version = "1.0-SNAPSHOT"

application {
    mainClass= "com.ramitsuri.gnucashreports.MainKt"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.kotlin.datetime)
    implementation(libs.kotlin.immutable.collections)
    implementation(libs.sqlite.jdbc)
    implementation(libs.serialization)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(17)
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    filter {
        exclude("**/generated/**")
        exclude("**/build/**")
        exclude { element -> element.file.toString().contains("generated/") }
        exclude { element -> element.file.toString().contains("build/") }
    }
}

tasks.shadowJar {
    minimize()
}
