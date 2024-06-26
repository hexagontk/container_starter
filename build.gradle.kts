import org.graalvm.buildtools.gradle.dsl.GraalVMExtension
import java.lang.System.getProperty

plugins {
    kotlin("jvm") version("1.9.23")
    id("org.graalvm.buildtools.native") version("0.10.1")
}

val hexagonVersion = "3.5.3"
val gradleScripts = "https://raw.githubusercontent.com/hexagontk/hexagon/$hexagonVersion/gradle"

ext.set("options", "-Xmx48m")
ext.set("applicationClass", "org.example.ApplicationKt")

apply(from = "$gradleScripts/kotlin.gradle")
apply(from = "$gradleScripts/application.gradle")
apply(from = "$gradleScripts/native.gradle")

defaultTasks("build")

version="1.0.0"
group="org.example"
description="Service's description"

dependencies {
    "implementation"("com.hexagonkt:http_server_helidon:$hexagonVersion")
//    "implementation"("org.slf4j:slf4j-nop:2.0.7")

    "testImplementation"("com.hexagonkt:http_client_jetty:$hexagonVersion")
}

extensions.configure<GraalVMExtension> {
    fun option(name: String, value: (String) -> String): String? =
        getProperty(name)?.let(value)

    binaries {
        named("main") {
            listOfNotNull(
                "--static", // Won't work on Windows or macOS
                "-R:MaxHeapSize=12",
                option("enableMonitoring") { "--enable-monitoring" },
            )
            .forEach(buildArgs::add)
        }
    }
}

tasks.register<Exec>("dockerBuild") {
    dependsOn("build", "zipNative", "zipJpackage")
    commandLine("docker-compose build --build-arg PROJECT=${project.name}".split(" "))
}
