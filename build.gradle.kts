plugins {
    `java-library`
    signing
    `maven-publish`
}

repositories {
    mavenLocal()
    mavenCentral()
}

// Set as appropriate for your organization
group = "de.zettsystems"
description = "Rewrite recipes from zettsystems."
version = "0.1.0"


java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

tasks {
    compileTestJava {
        sourceCompatibility = JavaVersion.VERSION_17.toString()
        targetCompatibility = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(platform("org.openrewrite.recipe:rewrite-recipe-bom:2.5.3"))
    implementation("org.openrewrite:rewrite-java")

    compileOnly("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

    runtimeOnly("org.openrewrite:rewrite-java-8")
    runtimeOnly("org.openrewrite:rewrite-java-17")
    runtimeOnly("ch.qos.logback:logback-classic:1.4.14")

    testImplementation("org.openrewrite:rewrite-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.1")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.1")
    testRuntimeOnly("com.google.guava:guava:33.0.0-jre")
}

// ------------------------------------------------------ sign & publish

val OSSRH_USERNAME: String by project
val OSSRH_PASSWORD: String by project

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://github.com/MichaelZett/zettsystems-recipes")
                licenses {
                    license {
                        name.set("Apache-2.0")
                        url.set("https://opensource.org/licenses/Apache-2.0")
                    }
                }
                developers {
                    developer {
                        id.set("michaelzett")
                        name.set("Michael Zoeller")
                        organization.set("ZettSystems")
                        organizationUrl.set("https://zettsystems.de/")
                    }
                }
                scm {
                    url.set("https://github.com/MichaelZett/zettsystems-recipes.git")
                    connection.set("scm:git:git://github.com/MichaelZett/zettsystems-recipes.git")
                    developerConnection.set("scm:git:git://github.com/MichaelZett/zettsystems-recipes.git")
                }
                issueManagement {
                    url.set("https://github.com/MichaelZett/zettsystems-recipes/issues")
                }
            }

            repositories {
                maven {
                    url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = OSSRH_USERNAME
                        password = OSSRH_PASSWORD
                    }
                }
            }
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications["maven"])
}
