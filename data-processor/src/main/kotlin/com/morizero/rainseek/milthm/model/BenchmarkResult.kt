package com.morizero.rainseek.milthm.model

import org.slf4j.LoggerFactory

data class BenchmarkResult(
    var title: String = "",
    var precision: Double = 0.0,
    var recall: Double = 0.0,
    var queryCount: Int = 0,
) {
    companion object {
        val logger = LoggerFactory.getLogger(BenchmarkResult::class.java)

        fun printHeading() {
            logger.info("title, precision, recall, f1")
        }

        fun BenchmarkResult.print() {
            logger.info(
                "$title, ${
                    "%.4f".format(precision)
                }, ${
                    "%.4f".format(recall)
                }, ${
                    "%.4f".format(this.f1())
                }"
            )        }
    }

    fun f1(): Double {
        return if (precision + recall == 0.0) {
            0.0
        } else {
            2 * precision * recall / (precision + recall)
        }
    }
}
