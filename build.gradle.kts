import com.vanniktech.maven.publish.SonatypeHost

plugins {
    java
    signing
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = "io.codef.api"
version = "2.0.0-ALPHA-001"

repositories {
    mavenCentral()
}

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    pom {
        name = "easycodef-java-v2"
        description = "Advanced EasyCodef Library for JDK"
        inceptionYear = "2024"
        url = "https://api.codef.io"

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }
        developers {
            developer {
                id = "happybean"
                name = "Haebin Byeon"
                email = "happybean@hecto.co.kr"
            }
        }

        scm {
            connection = "scm:git:git://github.com/codef-io/easycodef-java-v2.git"
            developerConnection = "scm:git:ssh://github.com/codef-io/easycodef-java-v2.git"
            url = "https://github.com/codef-io/easycodef-java-v2.git"
        }

        issueManagement {
            system = "GitHub"
            url = "https://github.com/codef-io/easycodef-java-v2/issues"
        }
    }
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
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("printVersion") {
    doLast {
        println(project.version)
    }
}