plugins {
    id("java")
    // Shadow Plugin Version 8+ nutzt meist diese ID
    id("com.gradleup.shadow") version "9.3.1"
}

group = "de.evoxy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "evoxyRepositorySnapshots"
        url = uri("https://repo.evoxy.de/snapshots")
    }
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("de.evoxy:easyjsonconfig:1.0.1-SNAPSHOT")
    implementation("de.evoxy:fluxsql:1.0.4-SNAPSHOT")
    implementation("net.kyori:adventure-api:4.26.1")
    implementation("io.javalin:javalin:6.7.0")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")

    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:3.5.0-SNAPSHOT")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.isFork = true
    options.forkOptions.executable = "/Users/maxhellwig/Library/Java/JavaVirtualMachines/corretto-21.0.10/Contents/Home/bin/javac"
}


tasks {
    shadowJar {
        relocate("de.evoxy.easyjsonconfig", "de.evoxy.plugin.libs.easyjsonconfig")
        relocate("org.asynchttpclient", "de.evoxy.plugin.libs.asynchttpclient")

        archiveClassifier.set("")
    }

    build {
        dependsOn(shadowJar) // Führt shadowJar automatisch beim "build" aus
    }
}

tasks.test {
    useJUnitPlatform()
}