package com.morizero.rainseek.milthm

import com.morizero.rainseek.milthm.task.ChartRenameTask
import com.morizero.rainseek.milthm.task.DataProcessTask
import com.morizero.rainseek.milthm.task.IdBasedRenameTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class DataProcessorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("Milthm Data-Process, Launched!")
        target.tasks.register("data-process", DataProcessTask::class.java)
        target.tasks.register("rename-chart", ChartRenameTask::class.java)
        target.tasks.register("rename-illustrations", IdBasedRenameTask::class.java, "illustrations", "illustration")
        target.tasks.register("rename-people", IdBasedRenameTask::class.java, "people", "people")
        target.tasks.register("rename-songs", IdBasedRenameTask::class.java, "songs", "song")

        target.tasks.register("rename").also {
            it.get().dependsOn(
                "rename-chart", "rename-illustrations", "rename-people", "rename-songs"
            )
        }
    }
}
