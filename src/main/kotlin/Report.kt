package ddol.compression.tests

import ddol.compression.tests.archivers.Archiver
import java.io.File
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.time.Duration
import kotlin.time.DurationUnit

class Report {
    private data class MeasurementKey(
        val archiverName: String,
        val payloadName: String,
        val specificCompressionLevel: SpecificCompressionLevel
    ) : Comparable<MeasurementKey> {
        override fun compareTo(other: MeasurementKey) = compareValuesBy(
            this,
            other,
            MeasurementKey::payloadName,
            MeasurementKey::archiverName,
            MeasurementKey::specificCompressionLevel
        )
    }

    private data class MeasurementValue(
        val compressionRatio: CompressionRatio,
        val compressionDuration: Duration,
        val uncompressionDuration: Duration
    )

    private val measurements: HashMap<MeasurementKey, MutableList<MeasurementValue>> = hashMapOf()

    fun report(
        archiver: Archiver,
        originalPayload: Payload,
        specificCompressionLevel: SpecificCompressionLevel,
        compressionRatio: CompressionRatio,
        compressionDuration: Duration,
        uncompressionDuration: Duration,
    ) {
        val key = MeasurementKey(
            archiverName = archiver.name,
            payloadName = originalPayload.name,
            specificCompressionLevel = specificCompressionLevel
        )

        val value = MeasurementValue(
            compressionRatio = compressionRatio,
            compressionDuration = compressionDuration,
            uncompressionDuration = uncompressionDuration
        )

        measurements.computeIfAbsent(key) { ArrayList() } += value
    }

    fun saveToFile(reportFile: File) {
        val reportRows = ArrayList<List<String>>()
        reportRows += listOf(
            "Payload Name",
            "Payload Size",
            "Archiver",
            "Compression Level",
            "Compression Ratio",
            "Space Saving",
            "AVG Compression Time (ms)",
            "STDDEV Compression Time (ms)",
            "AVG Uncompression Time (ms)",
            "STDDEV Uncompression Time (ms)",
        )

        measurements.entries.sortedBy { it.key }.forEach { (key, values) ->
            val (avgCompressionDuration, stdDevCompressionDuration) = computeMeanAndDeviation(values) {
                it.compressionDuration.toDouble(DurationUnit.MILLISECONDS)
            }

            val (avgUncompressionDuration, stdDevUncompressionDuration) = computeMeanAndDeviation(values) {
                it.uncompressionDuration.toDouble(DurationUnit.MILLISECONDS)
            }

            reportRows += listOf(
                key.payloadName,
                ensureEqualAndTakeFirst(values) { it.compressionRatio.uncompressed }.toString(),
                key.archiverName,
                key.specificCompressionLevel.toString(),
                ensureEqualAndTakeFirst(values) { it.compressionRatio.ratioAsText },
                ensureEqualAndTakeFirst(values) { it.compressionRatio.spaceSavingAsText },
                avgCompressionDuration.toFormattedString(),
                stdDevCompressionDuration.toFormattedString(),
                avgUncompressionDuration.toFormattedString(),
                stdDevUncompressionDuration.toFormattedString(),
            )
        }

        reportFile.writeText(reportRows.joinToString("\n") { it.joinToString(",") })
    }

    private fun <V1, V2> ensureEqualAndTakeFirst(values: List<V1>, transform: (V1) -> V2): V2 {
        val transformedValues = values.map(transform)

        val uniqueTransformedValues = transformedValues.toSet()
        check(uniqueTransformedValues.size == 1) { "Non-unique values found: $uniqueTransformedValues" }

        return uniqueTransformedValues.first()
    }

    private fun <V> computeMeanAndDeviation(values: List<V>, transform: (V) -> Double): Pair<Double, Double> {
        val valuesAsDouble = values.map(transform)

        val mean = valuesAsDouble.reduce { sum, duration -> sum + duration } / values.size
        val stdDev = sqrt(valuesAsDouble.sumOf { duration -> (mean - duration).pow(2) } / values.size)

        return mean to stdDev
    }

    private fun Double.toFormattedString(): String = String.format(Locale.US, "%.2f", this)
}
