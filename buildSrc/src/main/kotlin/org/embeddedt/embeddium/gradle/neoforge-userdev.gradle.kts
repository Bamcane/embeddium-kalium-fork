import net.neoforged.gradle.dsl.common.runs.run.Run
import java.util.List;

plugins {
    id("embeddium-common")
    id("net.neoforged.gradle.userdev")
}

val neoForgePr = if(rootProject.hasProperty("neoforge_pr")) rootProject.properties["neoforge_pr"].toString() else null

repositories {
    if(neoForgePr != null) {
        maven("https://prmaven.neoforged.net/NeoForge/pr" + neoForgePr) {
            content {
                includeModule("net.neoforged", "neoforge")
                includeModule("net.neoforged", "testframework")
            }
        }
    }
}


minecraft {
    accessTransformers {
        file(rootProject.file("src/main/resources/META-INF/accesstransformer.cfg"))
    }
}

if(project.hasProperty("parchment_version")) {
    val parchment_info = project.properties["parchment_version"].toString().split("-")
    subsystems {
        parchment {
            minecraftVersion = parchment_info[0]
            mappingsVersion = parchment_info[1]
        }
    }
}

runs {
    configureEach {
        systemProperty("forge.logging.console.level", "info")

        modSource(sourceSets["main"])
        List.of("compat").forEach { modSource(sourceSets[it]) }
    }
}

dependencies {
    implementation("net.neoforged:neoforge:${project.properties["forge_version"].toString()}")
}
