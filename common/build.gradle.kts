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

    modCompileOnly(group = "info.journeymap", name = "journeymap-api", version = journeymapVersion)
    modCompileOnly(group = "tech.thatgravyboat", name = "commonats", version = "2.0")
}
