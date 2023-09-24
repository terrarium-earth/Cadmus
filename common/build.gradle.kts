architectury {
    val enabledPlatforms: String by rootProject
    common(enabledPlatforms.split(","))
}

repositories {
    maven {
        name = "JourneyMap (Public)"
        url = uri("https://jm.gserv.me/repository/maven-public/")
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
    val journeymapVersion: String by project
    val minecraftVersion: String by project
    val createFabricVersion: String by project

    modCompileOnly(group = "info.journeymap", name = "journeymap-api", version = journeymapVersion)
    modCompileOnly(group = "tech.thatgravyboat", name = "commonats", version = "1.0")

    modCompileOnly(group = "com.simibubi.create", name = "create-fabric-$minecraftVersion", version = createFabricVersion)
}
