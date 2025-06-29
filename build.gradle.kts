
plugins {
    id("java")
    id("com.google.protobuf") version "0.9.4"
}

group = "com.unicity.sdk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Cryptography
    implementation("com.google.guava:guava:33.0.0-jre")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")

    // CBOR
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.17.0")

    // JSON
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    
    // Logging
    implementation("org.slf4j:slf4j-api:2.0.13")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers:1.19.8")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
    testImplementation("org.testcontainers:mongodb:1.19.8")
    testImplementation("org.slf4j:slf4j-simple:2.0.13")
}

tasks.test {
    useJUnitPlatform {
        excludeTags("integration")
    }
    maxHeapSize = "1024m"
}

tasks.register<Test>("integrationTest") {
    useJUnitPlatform {
        includeTags("integration")
    }
    maxHeapSize = "2048m"
    shouldRunAfter(tasks.test)
}
