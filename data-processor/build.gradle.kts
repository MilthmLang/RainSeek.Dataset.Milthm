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
}

gradlePlugin {
    plugins {
        create("h2cs") {
            id = "com.morizero.rainseek.milthm.data-processor"
            implementationClass = "com.morizero.rainseek.milthm.DataProcessorPlugin"
        }
    }
}
