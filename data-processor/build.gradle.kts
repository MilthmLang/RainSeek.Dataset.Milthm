plugins {
    kotlin("jvm") version "2.3.0"
    `java-gradle-plugin`
    `java-library`
}

group = "com.morizero.rainseek.milthm"
version = "0.0.1"

repositories {
    mavenCentral()
}

object depVer {
    val jackson = "3.1.2"
    val kotlinx = "1.11.0"
    val ktorm = "4.1.1"
}

dependencies {
    api("com.ibm.icu:icu4j:77.1")
    api("org.yaml:snakeyaml:+")

    api("tools.jackson.core:jackson-databind:${depVer.jackson}")
    api("tools.jackson.core:jackson-core:${depVer.jackson}")
    api("tools.jackson.dataformat:jackson-dataformat-yaml:${depVer.jackson}")
    api("tools.jackson.module:jackson-module-kotlin:${depVer.jackson}")

    api("org.ktorm:ktorm-core:${depVer.ktorm}")
    api("org.ktorm:ktorm-support-sqlite:${depVer.ktorm}")
    api("org.xerial:sqlite-jdbc:3.53.0.0")

    api("com.github.houbb:opencc4j:1.13.1")
    api("com.github.houbb:pinyin:0.4.0")

    api("org.apache.lucene:lucene-analyzers-kuromoji:8.11.4")

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
