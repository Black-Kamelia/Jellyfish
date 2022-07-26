import com.github.gradle.node.npm.task.NpmTask

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.github.node-gradle.node") version "3.3.0"
}

group = "com.kamelia"
version = project.properties["project.version"] as String

repositories {
    mavenCentral()
}

node {
    version.set("16.16.0") // lts version
    distBaseUrl.set("https://nodejs.org/dist")
    download.set(true)
    nodeProjectDir.set(file("${project.projectDir}/client"))
    workDir.set(file("${project.projectDir}/.gradle/nodejs"))
}

val npmClean = tasks.register<Delete>("npmClean") {
    delete(file("${project.projectDir}/client/node_modules"))
    delete(file("${project.projectDir}/client/dist"))
    delete(file("${project.projectDir}/server/src/main/resources/static"))
}

val npmBuild = tasks.register<NpmTask>("npmBuild") {
    dependsOn(tasks.npmInstall)
    npmCommand.set(listOf("run", "build"))
    ignoreExitValue.set(false)
}

val bundleClient = tasks.register<Copy>("bundleClient") {
    dependsOn(npmBuild)
    from("client/dist")
    into("server/src/main/resources/static")
}

tasks.clean {
    dependsOn(npmClean)
    delete(file("executables"))
}

tasks.build {
    dependsOn(bundleClient)
}
