package com.morizero.rainseek.milthm.task

import com.morizero.rainseek.milthm.model.Chart
import com.morizero.rainseek.milthm.utils.yamlMapper
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

@CacheableTask
abstract class ChartRenameTask : DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDir: DirectoryProperty

    @TaskAction
    fun execute() {
        val files = inputDir.get().asFile.resolve("charts").listFiles() ?: emptyArray()
        val songIdRegex = Regex("song_[0-9a-fA-F]{32}")
        var p = false
        for (file in files) {
            try {
                val chart = yamlMapper.readValue(file, Chart::class.java)
                val songId = chart.songId
                if (songId.startsWith("song_fly_to_meteor")) {
                    continue
                }
                if (songIdRegex.matches(songId)) {
                    continue
                }
                val songName = songId.substring("song_".length)
                val newFileName = "$songName-${chart.difficulty.lowercase()}.yaml"
                if (newFileName == file.name) {
                    continue
                }
                logger.warn("{} -> {}", file.name, newFileName)

                val destFile = file.parentFile.resolve(newFileName)
                file.renameTo(destFile)
                p = true
            } catch (e: Exception) {
                logger.error("fail to process, file: {}", file.absoluteFile, e)
                throw e
            }
        }
        if (!p) {
            logger.warn("no file renamed")
        }
    }
}
