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
    api("org.xerial", "sqlite-jdbc", "3.49.1.0")

    api("com.github.houbb", "opencc4j", "1.13.1")
    api("com.github.houbb", "pinyin", "0.4.0")

    api("org.apache.lucene","lucene-analyzers-kuromoji","8.11.4")

    api(platform("com.squareup.okhttp3:okhttp-bom:4.12.0"))
    api("com.squareup.okhttp3:okhttp")
    api("com.squareup.okhttp3:logging-interceptor")

    api("org.apache.commons:commons-lang3:3.17.0")
}

gradlePlugin {
    plugins {
        create("rainseek-dataset-milthm") {
            id = "com.morizero.rainseek.milthm.data-processor"
            implementationClass = "com.morizero.rainseek.milthm.DataProcessorPlugin"
        }
    }
}
