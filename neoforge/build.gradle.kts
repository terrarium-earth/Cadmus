architectury {
    neoForge()
}

val common: Configuration by configurations.creating {
    configurations.compileClasspath.get().extendsFrom(this)
    configurations.runtimeClasspath.get().extendsFrom(this)
    configurations["developmentNeoForge"].extendsFrom(this)
}

dependencies {
    val minecraftVersion: String by project
    val neoforgeVersion: String by project
    val reiVersion: String by project

    neoForge(group = "net.neoforged", name = "neoforge", version = neoforgeVersion)

//    modLocalRuntime(group = "me.shedaniel", name = "RoughlyEnoughItems-neoforge", version = reiVersion)
//    modLocalRuntime(group = "dev.architectury", name = "architectury-neoforge", version = "10.0.16")

    common(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    shadowCommon(project(path = ":common", configuration = "transformProductionNeoForge")) {
        isTransitive = false
    }

    forgeRuntimeLibrary("com.teamresourceful:yabn:1.0.3")
    forgeRuntimeLibrary("com.teamresourceful:bytecodecs:1.0.2")
}

tasks.processResources {
    inputs.property("version", version)

    filesMatching("META-INF/mods.toml") {
        expand("version" to version)
    }
}
