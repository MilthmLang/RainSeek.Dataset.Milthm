package com.morizero.rainseek.milthm

import org.gradle.api.Plugin
import org.gradle.api.Project

class DataProcessorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("Milthm Data-Process, Launched!")
        target.tasks.register("data-process", DataProcessTask::class.java)
    }
}
