plugins {
    id("java")
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("io.papermc.paperweight.userdev") version "1.3.8"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "de.lennox"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://libraries.minecraft.net")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    testCompileOnly("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")
    paperDevBundle("1.19.2-R0.1-SNAPSHOT")
    implementation("org.postgresql:postgresql:42.5.0")
}

tasks {
    assemble {
        dependsOn(reobfJar)
        dependsOn(shadowJar)
    }
    compileJava {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name() // We want UTF-8 for everything
    }
}

run {
    registerServerTask("1.19.2", 17)
}

fun registerServerTask(serverVersion: String, javaVersion: Int) {
    tasks.register<xyz.jpenilla.runpaper.task.RunServerTask>("server_${serverVersion}-j$javaVersion") {
        group = "Permissions"
        dependsOn("assemble")
        pluginJars.from("build/libs/${project.name}-${project.version}.jar")
        minecraftVersion(serverVersion)
        runDirectory(File("paper_${serverVersion}-j$javaVersion"))
        jvmArgs("-Dcom.mojang.eula.agree=true")
    }
}


java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

bukkit {
    name = "PlayerPermissions"
    description = "A player permission plugin with Groups"
    version = "${project.version}"
    main = "de.lennox.permissions.PlayerPermissionPlugin"
    apiVersion = "1.13"
    author = "Lennox"
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}