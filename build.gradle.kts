plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("xyz.jpenilla.run-paper") version "1.0.6"
}

group = "me.lorenzo0111"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven ("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven ("https://oss.sonatype.org/content/groups/public/")
    maven ("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven ("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.18.1-R0.1-SNAPSHOT")
    //compileOnly("com.github.CraftingStore.MinecraftPlugin:bukkit:master-SNAPSHOT")
    compileOnly("me.clip:placeholderapi:2.11.1")
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.xerial:sqlite-jdbc:3.36.0.3")
}

tasks {

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }

    processResources {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filteringCharset = Charsets.UTF_8.name()
    }

    runServer {
        minecraftVersion("1.19.3")
    }

    build {
        dependsOn("shadowJar")
    }
}