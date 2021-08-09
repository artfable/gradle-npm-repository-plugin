group = "com.artfable.gradle"
version = "0.0.5"

plugins {
    kotlin("jvm")
    `kotlin-dsl`
    `maven-publish`
    id("com.jfrog.artifactory") version "4.24.14"
    id("artfable.artifact") version "0.0.3"
}

repositories {
    mavenLocal()
    mavenCentral()
}

val jakson_version = "2.9.5"
val junit_version = "5.1.0"
val mockito_version = "2.17.0"

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("com.fasterxml.jackson.core:jackson-core:$jakson_version")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jakson_version")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
    testImplementation("org.mockito:mockito-core:$mockito_version")
    testImplementation("org.mockito:mockito-junit-jupiter:$mockito_version")
}

tasks.test {
    useJUnitPlatform()
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "11"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourceJar"])
            artifact(tasks["javadocJar"])

            pom {
                description.set("The plugin that allowed load frontend dependencies from package.json")
                licenses {
                    license {
                        name.set("MIT")
                        url.set("https://raw.githubusercontent.com/artfable/gradle-npm-repository-plugin/master/LICENSE")
                        distribution.set("repo")
                    }
                }
                developers {
                    developer {
                        id.set("artfable")
                        name.set("Artem Veselov")
                        email.set("art-fable@mail.ru")
                    }
                }
            }
        }
    }
}

artifactory {
    setContextUrl("https://artfable.jfrog.io/artifactory/")
    publish {
        repository {
            setRepoKey("default-maven-local")
            setUsername(artifactoryCredentials.user)
            setPassword(artifactoryCredentials.key)
        }
        defaults {
            publications ("mavenJava")

            setPublishArtifacts(true)
            setPublishPom(true)
            setPublishIvy(false)
        }
    }
}