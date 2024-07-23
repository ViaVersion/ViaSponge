plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
}

group = "com.viaversion"
version = "1.0.0"

dependencies {
    compileOnly("com.viaversion:viaversion-common:5.0.2-SNAPSHOT")
    compileOnly("com.viaversion:viabackwards-common:5.0.0-SNAPSHOT")
    compileOnly("com.viaversion:viarewind-common:4.0.0-SNAPSHOT")
    compileOnly("net.raphimc:viaaprilfools-common:3.0.1-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.0.20.Final")
    compileOnly("org.spongepowered:spongeapi:8.0.0")
    implementation("net.lenni0451:Reflect:1.3.2")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    shadowJar {
        archiveFileName.set("ViaSponge-${project.version}.jar")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
