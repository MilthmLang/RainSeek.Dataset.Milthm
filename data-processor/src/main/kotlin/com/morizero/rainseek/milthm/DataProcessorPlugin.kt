package com.morizero.rainseek.milthm

import com.morizero.rainseek.milthm.task.*
import org.gradle.api.Plugin
import org.gradle.api.Project

class DataProcessorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("Milthm Data-Process, Launched!")
        target.tasks.run {
            register("rename-chart", ChartRenameTask::class.java)
            register("rename-illustrations", IdBasedRenameTask::class.java, "illustrations", "illustration")
            register("rename-people", IdBasedRenameTask::class.java, "people", "people")
            register("rename-songs", IdBasedRenameTask::class.java, "songs", "song")

            register("data-load", LoadDataTask::class.java)
            register("data-saves", SaveToFiles::class.java)
            register("data-indexing", SaveToSqlite::class.java)

            register("benchmark", BenchmarkTask::class.java)

            register("rename").configure {
                it.dependsOn(
                    "rename-chart", "rename-illustrations", "rename-people", "rename-songs"
                )
            }

            named("data-saves").configure {
                it.dependsOn(
                    "data-load"
                )
            }

            named("data-indexing").configure {
                it.dependsOn(
                    "data-load"
                )
            }

            register("data-process").configure {
                it.dependsOn(
                    "data-load", "data-saves", "data-indexing"
                )
            }

            named("benchmark").configure {
                it.dependsOn(
                    "data-load"
                )
            }
        }
    }
}
