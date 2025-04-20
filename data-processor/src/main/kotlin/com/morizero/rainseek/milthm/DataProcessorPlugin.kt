package com.morizero.rainseek.milthm

import com.morizero.rainseek.milthm.task.ChartRenameTask
import com.morizero.rainseek.milthm.task.DataProcessTask
import com.morizero.rainseek.milthm.task.IdBasedRenameTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class DataProcessorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("Milthm Data-Process, Launched!")
        target.tasks.run {
            register("data-process", DataProcessTask::class.java)
            register("rename-chart", ChartRenameTask::class.java)
            register("rename-illustrations", IdBasedRenameTask::class.java, "illustrations", "illustration")
            register("rename-people", IdBasedRenameTask::class.java, "people", "people")
            register("rename-songs", IdBasedRenameTask::class.java, "songs", "song")

            register("rename").also {
                it.get().dependsOn(
                    "rename-chart", "rename-illustrations", "rename-people", "rename-songs"
                )
            }
        }
    }
}
