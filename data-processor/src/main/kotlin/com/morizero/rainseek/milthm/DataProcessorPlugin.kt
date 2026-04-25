package com.morizero.rainseek.milthm

import com.morizero.rainseek.milthm.task.*
import org.gradle.api.Plugin
import org.gradle.api.Project

class DataProcessorPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        println("Milthm Data-Process, Launched!")
        val inputDir = target.layout.projectDirectory.dir("src/main/resources/input")
        val outputDir = target.layout.buildDirectory.dir("output")

        target.tasks.run {
            register("rename-chart", ChartRenameTask::class.java) {
                it.inputDir.set(inputDir)
            }
            register("rename-illustrations", IdBasedRenameTask::class.java, "illustrations", "illustration").configure {
                it.inputDir.set(inputDir)
            }
            register("rename-people", IdBasedRenameTask::class.java, "people", "people").configure {
                it.inputDir.set(inputDir)
            }
            register("rename-songs", IdBasedRenameTask::class.java, "songs", "song").configure {
                it.inputDir.set(inputDir)
            }

            val dataLoad = register("data-load", LoadDataTask::class.java) {
                it.inputDir.set(inputDir)
            }
            register("data-saves", SaveToFiles::class.java) {
                it.loadDataTask.set(dataLoad)
                it.outputDir.set(outputDir)
            }
            register("data-indexing", SaveToSqlite::class.java) {
                it.loadDataTask.set(dataLoad)
                it.outputDir.set(outputDir)
            }

            register("benchmark", BenchmarkTask::class.java) {
                it.loadDataTask.set(dataLoad)
            }

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
