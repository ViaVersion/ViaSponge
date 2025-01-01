plugins {
    id("com.gradleup.shadow") version "8.3.5"
    id("java")
}

group = "com.viaversion"
version = "1.1.1"

dependencies {
    compileOnly("com.viaversion:viaversion-common:5.1.0")
    compileOnly("com.viaversion:viabackwards-common:5.2.0")
    compileOnly("com.viaversion:viarewind-common:4.0.2")
    compileOnly("net.raphimc:viaaprilfools-common:3.0.1")
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
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
