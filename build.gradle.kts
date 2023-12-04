plugins {
    id("org.openrewrite.build.recipe-library") version "latest.release"
}

// Set as appropriate for your organization
group = "de.zettsystems"
description = "Rewrite recipes."
version = "0.1.0"

// The bom version can also be set to a specific version or latest.release.
val latest = "latest.integration"
dependencies {
    implementation(platform("org.openrewrite:rewrite-bom:${latest}"))

    implementation("org.openrewrite:rewrite-java")
    runtimeOnly("org.openrewrite:rewrite-java-17")
    // Need to have a slf4j binding to see any output enabled from the parser.
    runtimeOnly("ch.qos.logback:logback-classic:1.2.+")

    testRuntimeOnly("com.google.guava:guava:latest.release")
}

configure<PublishingExtension> {
    publications {
        named("nebula", MavenPublication::class.java) {
            suppressPomMetadataWarningsFor("runtimeElements")
        }
    }
}

publishing {
  repositories {
      maven {
          name = "moderne"
          url = uri("https://us-west1-maven.pkg.dev/moderne-dev/moderne-recipe")
      }
  }
}
