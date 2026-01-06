import java.text.SimpleDateFormat
import java.util.*

plugins {
    id("java")
    id("idea")

    // Common
    id("org.spongepowered.gradle.vanilla") version ("0.2.2") apply (false)
    id("com.gradleup.shadow") version ("9.3.0") apply (false)

    // Fabric
    id("net.fabricmc.fabric-loom-remap") version ("1.14.10") apply (false)

    // Forge
    //id("net.minecraftforge.gradle") version ("[6.0.24,6.2)") apply (false)
    //id("org.spongepowered.mixin") version ("0.7+") apply (false)
}

val sharedProperties = readProperties(file("../shared.properties"))
val modVersion: String = System.getenv("INPUT_VERSION") ?: "0.0.0-dev"

subprojects {
    version = modVersion
    group = sharedProperties["modGroup"]!!

    apply {
        plugin("java")
        plugin("idea")
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion.set(JavaLanguageVersion.of(rootProject.extra["javaVersion"] as String))
    }

    tasks.jar {
        manifest {
            attributes["Specification-Title"] = sharedProperties["modName"]
            attributes["Specification-Vendor"] = sharedProperties["modAuthors"]
            attributes["Specification-Version"] = modVersion
            attributes["Implementation-Title"] = sharedProperties["modName"]
            attributes["Implementation-Vendor"] = sharedProperties["modAuthors"]
            attributes["Implementation-Version"] = modVersion
            attributes["Implementation-Timestamp"] = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(Date())
            attributes["Timestamp"] = System.currentTimeMillis()
            attributes["Built-On-Java"] = "${System.getProperty("java.vm.version")} (${System.getProperty("java.vm.vendor")})"
            attributes["Build-On-Minecraft"] = rootProject.extra["minecraftVersion"]
        }
    }

    tasks.processResources {
        filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "*.mixins.json")) { // "META-INF/mods.toml"
            expand(rootProject.properties + sharedProperties.map { it.key.toString() to it.value }.toMap() + mapOf("modVersion" to modVersion))
        }
    }
}
