plugins {
    java
}

group = "io.codef.api"
version = "2.0.0-ALPHA-01"

repositories {
    mavenCentral()
}

dependencies {

    /**
     * 2024-10-29 Latest
     * https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind
     */
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.1")

    /**
     * 2024-06-12 Latest
     * https://mvnrepository.com/artifact/commons-codec/commons-codec
     */
    implementation("commons-codec:commons-codec:1.17.1")

    /**
     * 2024-10-28 Latest
     * https://mvnrepository.com/artifact/org.apache.httpcomponents.client5/httpclient5/5.4.1
     * https://mvnrepository.com/artifact/org.apache.logging.log4j/log4j-slf4j2-impl
     */
    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.1")
    testImplementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.1")

    /**
     * 2024-10-21 Latest
     * https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-engine
     * https://mvnrepository.com/artifact/org.junit.jupiter/junit-jupiter-api
     */
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:all")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}