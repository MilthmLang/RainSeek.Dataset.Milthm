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

object depVer {
    val jackson = "2.15.2"
    val kotlinx = "1.6.3"
    val ktorm = "4.1.1"
}

dependencies {
    api("com.ibm.icu", "icu4j", "77.1")
    api("org.yaml", "snakeyaml", "+")

    api("com.fasterxml.jackson.core", "jackson-databind", depVer.jackson)
    api("com.fasterxml.jackson.module", "jackson-module-kotlin", depVer.jackson)
    api("com.fasterxml.jackson.dataformat", "jackson-dataformat-yaml", depVer.jackson)

    api("org.jetbrains.kotlinx", "kotlinx-serialization-json", depVer.kotlinx)

    api("org.ktorm", "ktorm-core", depVer.ktorm)
    api("org.ktorm", "ktorm-support-sqlite", depVer.ktorm)
}

gradlePlugin {
    plugins {
        create("rainseek-dataset-milthm") {
            id = "com.morizero.rainseek.milthm.data-processor"
            implementationClass = "com.morizero.rainseek.milthm.DataProcessorPlugin"
        }
    }
}
