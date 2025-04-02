package com.morizero.rainseek.milthm

import org.gradle.api.DefaultTask
import org.gradle.api.NonNullApi
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.TaskAction

@NonNullApi
@CacheableTask
open class DataProcessTask : DefaultTask() {
    @TaskAction
    fun execute() {
        TODO()
    }
}
