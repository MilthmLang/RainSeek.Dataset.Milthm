package com.morizero.rainseek.milthm.task

import com.morizero.rainseek.milthm.model.Chart
import com.morizero.rainseek.milthm.utils.yamlMapper
import org.gradle.api.DefaultTask
import org.gradle.api.NonNullApi
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File

@NonNullApi
@CacheableTask
open class ChartRenameTask : DefaultTask() {
    @Internal
    val resourceDirPath = File("${project.rootDir}/src/main/resources/input")

    @Internal
    val files: Array<out File> = resourceDirPath.resolve("charts").listFiles() ?: emptyArray()

    @TaskAction
    fun execute() {
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
