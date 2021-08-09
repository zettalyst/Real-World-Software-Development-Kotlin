plugins {
    kotlin("jvm") version "1.5.21"
}

group = "com.zettalyst"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.bouncycastle:bcprov-jdk15on:1.58")
    implementation("org.java-websocket:Java-WebSocket:1.5.1")
    implementation("org.eclipse.jetty:jetty-server:11.0.6")
    implementation("org.eclipse.jetty:jetty-servlet:11.0.6")
    implementation("com.fasterxml.jackson.core:jackson-core:2.12.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.12.4")
    implementation("org.mockito:mockito-core:2.21.0")
    testImplementation("junit:junit:4.11")
    testImplementation(platform("org.junit:junit-bom:5.7.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:1.12.0")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}