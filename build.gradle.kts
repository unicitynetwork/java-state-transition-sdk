plugins {
    id("java-library")
    id("maven-publish")
    id("checkstyle")
    id("com.google.protobuf") version "0.9.4"
    id("ru.vyarus.animalsniffer") version "2.0.1"
}

group = "org.unicitylabs"
// Use version property if provided, otherwise use default
version = if (project.hasProperty("version")) {
    project.property("version").toString()
} else {
    "1.1-SNAPSHOT"
}

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
    testImplementation("org.awaitility:awaitility:4.2.0")
    testImplementation("org.slf4j:slf4j-simple:2.0.13")
    testImplementation("com.google.guava:guava:33.0.0-jre")

    // ✅ Cucumber for BDD
    testImplementation("io.cucumber:cucumber-java:7.27.2")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:7.27.2")

    // JUnit 5 Suite annotations
    testImplementation("org.junit.platform:junit-platform-suite:1.13.4")

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
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
    systemProperties(System.getProperties().toMap() as Map<String, Any>)

    filter {
        excludeTestsMatching("*CucumberTestRunner*")
        excludeTestsMatching("*Cucumber*")
    }
}

tasks.withType<Checkstyle> {
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
    systemProperty("cucumber.junit-platform.naming-strategy", "long")

    filter {
        excludeTestsMatching("*CucumberTestRunner*")
        excludeTestsMatching("*Cucumber*")
    }
}

tasks.register<Test>("tokenTests") {
    useJUnitPlatform()
    maxHeapSize = "1024m"
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
    // Set the system properties first
    systemProperties = System.getProperties().toMap() as Map<String, Any>
    // Then override the specific cucumber filter (this will take precedence)
    systemProperty("cucumber.filter.tags", "@token-transfer")

    filter {
        includeTestsMatching("*CucumberTestRunner*")
    }
    shouldRunAfter(tasks.test)
}

tasks.register<Test>("aggregatorTests") {
    useJUnitPlatform()
    maxHeapSize = "1024m"
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
    // Set the system properties first
    systemProperties = System.getProperties().toMap() as Map<String, Any>
    systemProperty("cucumber.filter.tags", "@aggregator-connectivity")

    filter {
        includeTestsMatching("*CucumberTestRunner*")
    }
    shouldRunAfter(tasks.test)
}

tasks.register<Test>("advancedTokenTests") {
    useJUnitPlatform()
    maxHeapSize = "1024m"
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
    // Set the system properties first
    systemProperties = System.getProperties().toMap() as Map<String, Any>
    systemProperty("cucumber.filter.tags", "@advanced-token")

    filter {
        includeTestsMatching("*CucumberTestRunner*")
    }
    shouldRunAfter(tasks.test)
}

// ✅ Run all cucumber tests (including integration)
tasks.register<Test>("allCucumberTests") {
    useJUnitPlatform()
    maxHeapSize = "1024m"
    systemProperty("cucumber.junit-platform.naming-strategy", "long")
    systemProperties = System.getProperties().toMap() as Map<String, Any>
    systemProperty("cucumber.filter.tags", "not @ignore")

    filter {
        includeTestsMatching("*CucumberTestRunner*")
    }
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
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = "java-state-transition-sdk"
            version = project.version.toString()

            // Use the Java component as base - this includes the standard JAR
            from(components["java"])

            // Add Android JAR as additional artifact with classifier
            artifact(tasks["androidJar"]) {
                classifier = "android"
            }

            // Add JVM JAR as additional artifact with classifier
            artifact(tasks["jvmJar"]) {
                classifier = "jvm"
            }

            // Simple POM configuration without XML manipulation
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

                scm {
                    connection.set("scm:git:git://github.com/unicitynetwork/java-state-transition-sdk.git")
                    developerConnection.set("scm:git:ssh://github.com/unicitynetwork/java-state-transition-sdk.git")
                    url.set("https://github.com/unicitynetwork/java-state-transition-sdk")
                }
            }
        }
    }
}