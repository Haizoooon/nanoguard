plugins {
    id("java")
}

group = "de.evoxy"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        name = "evoxyRepositorySnapshots"
        url = uri("https://repo.evoxy.de/snapshots")
    }
}

dependencies {
    implementation("de.evoxy:easyjsonconfig:1.0.1-SNAPSHOT")
    implementation("de.evoxy:fluxsql:1.0.4-SNAPSHOT")
    implementation("com.google.code.gson:gson:2.13.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}