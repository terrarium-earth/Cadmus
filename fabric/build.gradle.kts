architectury {
    fabric()
}

val common: Configuration by configurations.creating {
    configurations.compileClasspath.get().extendsFrom(this)
    configurations.runtimeClasspath.get().extendsFrom(this)
    configurations["developmentFabric"].extendsFrom(this)
}

repositories {
    maven {
        url = uri("https://maven.nucleoid.xyz/")
        content {
            includeGroup("eu.pb4")
        }
    }
}

dependencies {
    common(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    shadowCommon(project(path = ":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }

    val minecraftVersion: String by project
    val fabricLoaderVersion: String by project
    val fabricApiVersion: String by project
    val commonProtectionApiVersion: String by project
    val xaerosWorldMapVersion: String by project
    val xaerosMiniMapVersion: String by project

    modImplementation(group = "net.fabricmc", name = "fabric-loader", version = fabricLoaderVersion)
    modApi(group = "net.fabricmc.fabric-api", name = "fabric-api", version = "$fabricApiVersion+$minecraftVersion")
    include(modImplementation(group = "eu.pb4", name = "common-protection-api", version = commonProtectionApiVersion))

    // modLocalRuntime(group = "maven.modrinth", name = "xaeros-world-map", version = "${xaerosWorldMapVersion}_Fabric_$minecraftVersion")
    // modLocalRuntime(group = "maven.modrinth", name = "xaeros-minimap", version = "${xaerosMiniMapVersion}_Fabric_$minecraftVersion")
}
