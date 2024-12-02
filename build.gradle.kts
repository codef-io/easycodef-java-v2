import com.vanniktech.maven.publish.JavaLibrary
import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.SonatypeHost

plugins {
    java
    signing
    id("com.vanniktech.maven.publish") version "0.30.0"
}


group = "io.codef.api"
version = "2.0.0-beta-005"

signing {
    useInMemoryPgpKeys(
        System.getenv("GPG_PRIVATE_KEY"),
        System.getenv("GPG_PASSPHRASE")
    )
    sign(publishing.publications)
}

repositories {
    mavenCentral()
}

mavenPublishing {
    configure(JavaLibrary(JavadocJar.Javadoc(), true))
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    coordinates("io.codef.api", "easycodef-java-v2", version.toString())
    pom {
        url = "api.codef.io"
        name = "easycodef-java-v2"
        description = "Advanced EasyCodef Library for Java"
        inceptionYear = "2024"

        licenses {
            license {
                name = "MIT License"
                url = "https://opensource.org/licenses/MIT"
                distribution = "https://opensource.org/licenses/MIT"
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
            connection = "scm:git:git@github.com:codef-io/easycodef-java-v2.git"
            developerConnection = "scm:git:ssh://github.com/codef-io/easycodef-java-v2.git"
            url = "https://github.com/codef-io/easycodef-java-v2"
        }

        issueManagement {
            system = "GitHub"
            url = "https://github.com/codef-io/easycodef-java-v2/issues"
        }
    }
}


dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
    implementation("com.alibaba:fastjson:2.0.53")
    implementation("commons-codec:commons-codec:1.17.1")


    implementation("org.apache.httpcomponents.client5:httpclient5:5.4.1")
    implementation("ch.qos.logback:logback-classic:1.5.12")
    implementation("ch.qos.logback:logback-core:1.5.12")
    implementation("org.slf4j:slf4j-api:2.0.16")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.11.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.11.3")
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
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