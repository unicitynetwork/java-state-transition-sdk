plugins {
    id("java-library")
    id("maven-publish")
    id("com.google.protobuf") version "0.9.4"
}

group = "com.unicity.sdk"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Define configurations for different flavors
configurations {
    create("android")
    create("jvm")
}

dependencies {
    // Core dependencies that work on both platforms
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.17.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
    implementation("org.bouncycastle:bcprov-jdk15on:1.70")
    implementation("org.bouncycastle:bcpkix-jdk15on:1.70")
    implementation("org.slf4j:slf4j-api:2.0.13")
    
    // Platform-specific Guava
    compileOnly("com.google.guava:guava:33.0.0-jre")
    "android"("com.google.guava:guava:33.0.0-android")
    "jvm"("com.google.guava:guava:33.0.0-jre")
    
    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers:1.19.8")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
    testImplementation("org.testcontainers:mongodb:1.19.8")
    testImplementation("org.slf4j:slf4j-simple:2.0.13")
    testImplementation("com.google.guava:guava:33.0.0-jre")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
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

// Create separate JARs for each platform
tasks.register<Jar>("androidJar") {
    archiveClassifier.set("android")
    from(sourceSets["main"].output)
    manifest {
        attributes["Target-Platform"] = "Android"
    }
}

tasks.register<Jar>("jvmJar") {
    archiveClassifier.set("jvm")
    from(sourceSets["main"].output)
    manifest {
        attributes["Target-Platform"] = "JVM"
    }
}

// Publishing configuration
publishing {
    publications {
        create<MavenPublication>("android") {
            artifactId = "unicity-sdk-android"
            from(components["java"])
            artifact(tasks["androidJar"])
            
            pom {
                name.set("Unicity SDK for Android")
                description.set("Unicity State Transition SDK for Android 12+")
                
                withXml {
                    val dependenciesNode = asNode().appendNode("dependencies")
                    
                    configurations["implementation"].dependencies.forEach { dep ->
                        if (dep.group != "com.google.guava") {
                            val dependencyNode = dependenciesNode.appendNode("dependency")
                            dependencyNode.appendNode("groupId", dep.group)
                            dependencyNode.appendNode("artifactId", dep.name)
                            dependencyNode.appendNode("version", dep.version)
                            dependencyNode.appendNode("scope", "compile")
                        }
                    }
                    
                    // Add Android-specific Guava
                    val guavaDep = dependenciesNode.appendNode("dependency")
                    guavaDep.appendNode("groupId", "com.google.guava")
                    guavaDep.appendNode("artifactId", "guava")
                    guavaDep.appendNode("version", "33.0.0-android")
                    guavaDep.appendNode("scope", "compile")
                }
            }
        }
        
        create<MavenPublication>("jvm") {
            artifactId = "unicity-sdk"
            from(components["java"])
            artifact(tasks["jvmJar"])
            
            pom {
                name.set("Unicity SDK")
                description.set("Unicity State Transition SDK for JVM")
            }
        }
    }
}