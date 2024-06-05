plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("java")
    id("maven-publish")
}

group = "com.viaversion"
version = "1.0.0"

dependencies {
    compileOnly("com.viaversion:viaversion-common:5.0.0-SNAPSHOT")
    compileOnly("com.viaversion:viabackwards-common:5.0.0-SNAPSHOT")
    compileOnly("com.viaversion:viarewind-common:4.0.0-SNAPSHOT")
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


publishing {
    publications.create<MavenPublication>("mavenJava") {
        groupId = rootProject.group as String
        artifactId = "sponge"
        version = rootProject.version as String

        artifact(tasks["shadowJar"])
    }
    repositories.maven {
        name = "Via"
        url = uri("https://repo.viaversion.com/")
        credentials(PasswordCredentials::class)
        authentication {
            create<BasicAuthentication>("basic")
        }
    }
}
