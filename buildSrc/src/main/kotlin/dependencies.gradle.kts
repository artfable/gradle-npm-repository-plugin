plugins {
    kotlin("jvm")
}

val jakson_version = "2.9.5"
val junit_version = "5.1.0"
val mockito_version = "2.17.0"

dependencies {
    constraints {
        implementation("com.fasterxml.jackson.core:jackson-core:$jakson_version")
        implementation("com.fasterxml.jackson.core:jackson-databind:$jakson_version")

        testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
        testImplementation("org.mockito:mockito-core:$mockito_version")
        testImplementation("org.mockito:mockito-junit-jupiter:$mockito_version")
    }
}