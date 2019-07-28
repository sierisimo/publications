plugins {
    kotlin("js") version "1.3.41"
    id("com.gtramontina.ghooks.gradle") version "1.1.0"
}

group = "com.sierisimo"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-js"))
}

kotlin.target.nodejs {

}

tasks.register<Copy>("createServer") {
    dependsOn("build")

    from("$buildDir/js/packages/even_server/kotlin/even_server.js")
    into(buildDir)
}