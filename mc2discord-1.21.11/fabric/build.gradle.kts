plugins {
    id("net.fabricmc.fabric-loom-remap")
    id("com.gradleup.shadow")
}

project.gradle.startParameter.excludedTaskNames.add("test")

val sharedProperties = readProperties(file("../../shared.properties"))

base {
    archivesName.set("${sharedProperties["modId"]}-fabric-${rootProject.extra["minecraftDisplayVersion"]}")
}

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

configurations.implementation.get().extendsFrom(configurations.shadow.get())
dependencies {
    minecraft("com.mojang:minecraft:${rootProject.extra["minecraftVersion"]}")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:${rootProject.extra["fabricLoaderVersion"]}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${rootProject.extra["fabricVersion"]}")
    shadow(project(":common"))
    shadow(project(":mc2discord-core"))
}

java {
	// Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
	// if it is present.
	// If you remove this line, sources will not be generated.
	withSourcesJar()

	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

loom {
    accessWidenerPath.set(project(":common").file("src/main/resources/${sharedProperties["modId"]}.accesswidener"))
    // When set only server related features and jars will be setup.
	serverOnlyMinecraftJar()

    @Suppress("UnstableApiUsage")
    mixin {
        useLegacyMixinAp.set(true)
        defaultRefmapName.set("${sharedProperties["modId"]}.refmap.json")
    }

    runs {
        register("FabricServer") {
            server()
            ideConfigGenerated(true)
            name("Fabric Server")
        }
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        source(project(":common").sourceSets.main.get().allSource)
    }

    processResources {
        from(project(":common").sourceSets.main.get().resources)
    }

    shadowJar {
        configurations = listOf(project.configurations.shadow.get())
        val relocateLocation = "${sharedProperties["modGroup"]}.shadow"
        val relocations = listOf(
            "io.netty",
            "reactor",
            "discord4j",
            "org.reactivestreams",
            "org.checkerframework",
            "com.iwebpp.crypto",
            "com.google.errorprone",
            "com.github.benmanes.caffeine",
            "com.fasterxml.jackson",
            "com.discord4j.fsm",
            "com.austinv11.servicer",
            "com.vdurmont.emoji",
            "fr.denisd3d.config4j",
            "org.apache.commons.collections4",
            "org.immutables.encode",
            "org.json",
            "com.electronwill.nightconfig"
        )
        relocations.forEach {
            relocate(it, "$relocateLocation.$it")
        }
        exclude("META-INF/services/**") // Fix compatibility with geckolib
    }

    remapJar {
        dependsOn(shadowJar)
        inputFile.set(shadowJar.get().archiveFile.get())
    }
}
