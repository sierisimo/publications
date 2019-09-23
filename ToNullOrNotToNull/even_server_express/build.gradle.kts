plugins {
    kotlin("js") version "1.3.50"
    //id("com.gtramontina.ghooks.gradle") version "1.1.0"
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
    group = "Custom server tasks"
    description = "Create the js file to use"

    dependsOn("build")

    from("$buildDir/js/packages/even_server/kotlin/even_server.js")
    into(buildDir)
}

tasks.register<Exec>("runServer") {
    group = "Custom server tasks"
    description = "Run the created server"

    dependsOn("createServer")

    commandLine("npm", "start")
}