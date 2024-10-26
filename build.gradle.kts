import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.ir.backend.js.compile
import org.jetbrains.kotlin.utils.addToStdlib.ifTrue

plugins {
    id("java")
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

kotlin {
    jvmToolchain(8)
}

group = "cc.mcyx.arona"
version = "1.0"

val aronaVersion = "1.0.0"
val arona = "cc.mcyx:Arona"

fun aronaModule(modelName: String, version: String = aronaVersion): String {
    return "cc.mcyx:${modelName}:${version}"
}

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://hub.spigotmc.org/nexus/content/groups/public/")
}
val inAronaModule = listOf(
    aronaModule("Arona"),
    aronaModule("Arona-GUI"),
    aronaModule("Arona-NMS"),
)
dependencies {
    inAronaModule.forEach { implementation(it) }
    compileOnly("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    implementation("cn.hutool:hutool-all:5.8.32")
    implementation(files("lib/PlayerPoints.jar"))
}


val fastDev = true
val devDir = "E:\\Desktop\\Desktop\\MinecraftServers\\CatServer1.12.2\\plugins"
tasks.withType<ShadowJar> {
    dependencies { inAronaModule.forEach { include(dependency(it)) } }
    doLast {
        fastDev.ifTrue {
            archiveFile.get().asFile.also {
                it.renameTo(File(devDir, it.name).apply { delete() })
            }
        }
    }
}