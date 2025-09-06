plugins {
    id("java-library")
    id("maven-publish")
    id("checkstyle")
    id("com.google.protobuf") version "0.9.4"
    id("ru.vyarus.animalsniffer") version "2.0.1"
}

group = "com.github.unicitynetwork"
// JitPack will override this with the tag version via -Pversion
version = project.findProperty("version") ?: "1.1-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

// Define configurations for different flavors
configurations {
    create("android")
    create("jvm")
}

dependencies {
    // Core dependencies that work on both platforms
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jdk8:2.19.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-cbor:2.19.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")
    implementation("org.bouncycastle:bcprov-jdk18on:1.81")
    implementation("org.slf4j:slf4j-api:2.0.13")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Platform-specific Guava
    compileOnly("com.google.guava:guava:33.0.0-jre")
    "android"("com.google.guava:guava:33.0.0-android")
    "jvm"("com.google.guava:guava:33.0.0-jre")

    // Animal Sniffer signatures
    signature("com.toasttab.android:gummy-bears-api-34:0.7.0@signature")

    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.testcontainers:testcontainers:1.19.8")
    testImplementation("org.testcontainers:junit-jupiter:1.19.8")
    testImplementation("org.testcontainers:mongodb:1.19.8")
    testImplementation("org.slf4j:slf4j-simple:2.0.13")
    testImplementation("com.google.guava:guava:33.0.0-jre")

    checkstyle("com.puppycrawl.tools:checkstyle:10.26.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
    withJavadocJar()
}

checkstyle {
    configFile = file("config/checkstyle/checkstyle.xml")
}

tasks.test {
    useJUnitPlatform {
        excludeTags("integration")
    }
    maxHeapSize = "1024m"
}

tasks.withType<Checkstyle>{
    reports {
        xml.required.set(false)
        html.required.set(true)
    }
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


// Publishing configuration for JitPack
publishing {
    publications {
        // Main publication (Android-compatible by default for backward compatibility)
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "java-state-transition-sdk"
            version = project.version.toString()
            
            from(components["java"])
            
            // Replace the default JAR with Android JAR as the main artifact
            artifacts.clear()
            artifact(tasks["androidJar"])
            
            // Add JVM JAR as a classifier variant
            artifact(tasks["jvmJar"]) {
                classifier = "jvm"
            }
            
            // Add sources and javadoc
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            
            pom {
                name.set("Unicity State Transition SDK")
                description.set("Unicity State Transition SDK for Android and JVM")
                url.set("https://github.com/unicitynetwork/java-state-transition-sdk")
                
                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                
                developers {
                    developer {
                        id.set("unicitynetwork")
                        name.set("Unicity Network")
                    }
                }
                
                // Modify existing dependencies to use Android Guava
                withXml {
                    val root = asNode()
                    val dependenciesNodes = root.children().filter { node ->
                        (node as? groovy.util.Node)?.name()?.toString()?.endsWith("dependencies") == true
                    }
                    
                    dependenciesNodes.forEach { dependencies ->
                        val depNode = dependencies as groovy.util.Node
                        
                        // Find and update Guava dependency
                        depNode.children().forEach { dep ->
                            val dependency = dep as groovy.util.Node
                            val groupIdNode = dependency.children().find { 
                                (it as? groovy.util.Node)?.name()?.toString()?.endsWith("groupId") == true 
                            } as? groovy.util.Node
                            val artifactIdNode = dependency.children().find { 
                                (it as? groovy.util.Node)?.name()?.toString()?.endsWith("artifactId") == true 
                            } as? groovy.util.Node
                            
                            if (groupIdNode?.text() == "com.google.guava" && artifactIdNode?.text() == "guava") {
                                val versionNode = dependency.children().find { 
                                    (it as? groovy.util.Node)?.name()?.toString()?.endsWith("version") == true 
                                } as? groovy.util.Node
                                versionNode?.setValue("33.0.0-android")
                            }
                        }
                    }
                }
            }
        }
    }
}