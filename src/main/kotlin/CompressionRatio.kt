@file:Suppress("MemberVisibilityCanBePrivate")

package ddol.compression.tests

import ddol.compression.tests.Size.Companion.ZERO
import java.text.DecimalFormat

class CompressionRatio(val uncompressed: Size, val compressed: Size) {
    val ratio: Double? = if (uncompressed == ZERO || compressed == ZERO)
        null
    else
        uncompressed.bytes.toDouble() / compressed.bytes.toDouble()

    val spaceSaving: Double? = ratio?.let { 1.0 - 1.0 / it }

    val ratioAsText: String = ratio?.let { "${RATIO_FORMATTER.format(it)}:1" } ?: "?"
    val spaceSavingAsText: String = spaceSaving?.let { "${(it * 100).toULong()}%" } ?: "?"

    companion object {
        private val RATIO_FORMATTER = DecimalFormat("#.##")
    }
}
