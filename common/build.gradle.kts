architectury {
    val enabledPlatforms: String by rootProject
    common(enabledPlatforms.split(","))
}

repositories {
    maven {
        name = "JourneyMap (Public)"
        url = uri("https://jm.gserv.me/repository/maven-public/")
    }
}

dependencies {
    val journeymapVersion: String by project

    modCompileOnly(group = "tech.thatgravyboat", name = "commonats", version = "2.0")
    modCompileOnly(group = "maven.modrinth", name = "xaeros-world-map", version = "1.39.0_Fabric_1.21")
}
