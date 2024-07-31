plugins {
    id("io.papermc.paperweight.userdev") version "1.7.1"
    java
    //id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "xyz.acrylicstyle"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/public/") }
}

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

dependencies {
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
    paperweight.paperDevBundle("1.20.2-R0.1-SNAPSHOT")
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
    }
}
