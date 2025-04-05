plugins {
    kotlin("jvm") version "2.1.10"
    `java-gradle-plugin`
    `java-library`
}

group = "com.morizero.rainseek.milthm"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    api("com.ibm.icu", "icu4j", "77.1")
    api("org.yaml:snakeyaml:+")
    api("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")
    api("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.15.2")
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

gradlePlugin {
    plugins {
        create("h2cs") {
            id = "com.morizero.rainseek.milthm.data-processor"
            implementationClass = "com.morizero.rainseek.milthm.DataProcessorPlugin"
        }
    }
}
