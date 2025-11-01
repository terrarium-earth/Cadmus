architectury {
    val enabledPlatforms: String by rootProject
    common(enabledPlatforms.split(","))
}

repositories {
    maven {
        name = "JourneyMap (Public)"
        url = uri("https://jm.gserv.me/repository/maven-public/")
    }
    mavenCentral()
}

dependencies {
    val xaerosWorldMapVersion: String by project
    val commonatsVersion: String by project
    val minecraftVersion: String by project

    modCompileOnly(group = "tech.thatgravyboat", name = "commonats", version = commonatsVersion)
    modCompileOnly(group = "maven.modrinth", name = "xaeros-world-map", version = "${xaerosWorldMapVersion}_Fabric_$minecraftVersion")
}