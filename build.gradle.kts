plugins {
    id("com.gradleup.shadow") version "8.3.0"
    id("java")
}

group = "com.viaversion"
version = "1.0.2"

dependencies {
    compileOnly("com.viaversion:viaversion-common:5.0.3")
    compileOnly("com.viaversion:viabackwards-common:5.0.3")
    compileOnly("com.viaversion:viarewind-common:4.0.2")
    compileOnly("net.raphimc:viaaprilfools-common:3.0.1")
    compileOnly("io.netty:netty-all:4.0.20.Final")
    compileOnly("org.spongepowered:spongeapi:8.0.0")
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
