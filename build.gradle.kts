plugins {
    id("com.gradleup.shadow") version "9.3.2"
    id("java")
}

group = "com.viaversion"
version = "1.4.0"

dependencies {
    compileOnly("com.viaversion:viaversion-common:5.9.0")
    compileOnly("com.viaversion:viabackwards-common:5.9.0")
    compileOnly("com.viaversion:viarewind-common:4.1.0")
    compileOnly("com.viaversion:viaaprilfools-common:4.2.0")
    compileOnly("io.netty:netty-all:4.1.112.Final")
    compileOnly("org.spongepowered:spongeapi:8.0.0")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveFileName.set("ViaSponge-${project.version}.jar")
    }

    processResources {
        val projectVersion = project.version
        filesMatching("META-INF/sponge_plugins.json") {
            expand(mapOf("version" to projectVersion))
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
