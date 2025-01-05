plugins {
    id("com.gradleup.shadow") version "8.3.5"
    id("java")
}

group = "com.viaversion"
version = "1.2.0"

dependencies {
    compileOnly("com.viaversion:viaversion-common:5.2.1")
    compileOnly("com.viaversion:viabackwards-common:5.2.1")
    compileOnly("com.viaversion:viarewind-common:4.0.5")
    compileOnly("com.viaversion:viaaprilfools-common:4.0.0")
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
