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
    maven {
        name = "devOS Snapshots"
        url = uri("https://mvn.devos.one/snapshots/")
    }
    maven {
        name = "Jitpack"
        url = uri("https://jitpack.io/")
    }
    maven {
        url = uri("https://maven.jamieswhiteshirt.com/libs-release")
        content {
            includeGroup("com.jamieswhiteshirt")
        }
    }
    maven {
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
}

dependencies {
    val minecraftVersion: String by project
    val fabricLoaderVersion: String by project
    val fabricApiVersion: String by project
    val commonProtectionApiVersion: String by project
    val createFabricVersion: String by project

    modImplementation(group = "net.fabricmc", name = "fabric-loader", version = fabricLoaderVersion)
    modApi(group = "net.fabricmc.fabric-api", name = "fabric-api", version = "$fabricApiVersion+$minecraftVersion")
    include(modImplementation(group = "eu.pb4", name = "common-protection-api", version = commonProtectionApiVersion))

    common(project(":common", configuration = "namedElements")) {
        isTransitive = false
    }
    shadowCommon(project(path = ":common", configuration = "transformProductionFabric")) {
        isTransitive = false
    }

    modImplementation(group = "com.simibubi.create", name = "create-fabric-$minecraftVersion", version = createFabricVersion)
}

tasks.processResources {
    inputs.property("version", version)

    filesMatching("fabric.mod.json") {
        expand("version" to version)
    }
}