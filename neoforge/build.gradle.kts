architectury {
    neoForge()
}

val common: Configuration by configurations.creating {
    configurations.compileClasspath.get().extendsFrom(this)
    configurations.runtimeClasspath.get().extendsFrom(this)
    configurations["developmentNeoForge"].extendsFrom(this)
}

dependencies {
    common(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    shadowCommon(project(path = ":common", configuration = "transformProductionNeoForge")) {
        isTransitive = false
    }

    val minecraftVersion: String by project
    val neoforgeVersion: String by project
    val xaerosWorldMapVersion: String by project
    val xaerosMiniMapVersion: String by project
    val byteCodecsVersion: String by project

    neoForge(group = "net.neoforged", name = "neoforge", version = neoforgeVersion)

    forgeRuntimeLibrary(group = "com.teamresourceful", name = "bytecodecs", version = byteCodecsVersion)

    modLocalRuntime(group = "maven.modrinth", name = "xaeros-world-map", version = "${xaerosWorldMapVersion}_NeoForge_$minecraftVersion")
    modLocalRuntime(group = "maven.modrinth", name = "xaeros-minimap", version = "${xaerosMiniMapVersion}_NeoForge_$minecraftVersion")
}
