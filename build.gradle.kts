plugins {
    id("com.morizero.rainseek.milthm.data-processor")
}

group = "com.morizero.rainseek"
version = "0.0.1"

subprojects {
    group = "com.morizero.rainseek"
    version = "0.0.1"
    repositories { repo() }
}

repositories { repo() }

fun RepositoryHandler.repo() {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://plugins.gradle.org/m2/") }
}

tasks {
    benchmark {
        endPoint = System.getProperty("benchmark.endPoint") ?: throw Exception("No endpoint given")
    }
}
