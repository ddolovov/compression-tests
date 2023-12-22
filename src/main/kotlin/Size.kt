@file:Suppress("MemberVisibilityCanBePrivate")

package ddol.compression.tests

import java.io.File
import java.text.DecimalFormat

@JvmInline
value class Size(val bytes: ULong) {
    operator fun plus(other: Size): Size = Size(bytes + other.bytes)

    override fun toString(): String {
        if (this == ZERO) return "0 B"

        val bytesAsDouble = bytes.toDouble()

        val (unitsAsDouble, unitName) = SIZE_UNITS.mapIndexedNotNull { index, unitName ->
            val unitsAsDouble = bytesAsDouble / (1uL shl (index * 10)).toDouble()
            if (unitsAsDouble < 1.0) return@mapIndexedNotNull null else unitsAsDouble to unitName
        }.last()

        return "${UNITS_FORMATTER.format(unitsAsDouble)} $unitName"
    }

    companion object {
        val ZERO = Size(0uL)

        private val SIZE_UNITS: List<String> = listOf("B", "KB", "MB", "GB")
        private val UNITS_FORMATTER = DecimalFormat("#.##")
    }
}

fun getSizeOf(fileOrDir: File): Size = when {
    fileOrDir.isFile -> getSizeOfFile(fileOrDir)
    fileOrDir.isDirectory -> getSizeOfDir(fileOrDir)
    !fileOrDir.exists() -> error("Does not exist: $fileOrDir")
    else -> error("Not a file or a directory: $fileOrDir")
}

private fun getSizeOfFile(file: File): Size {
    check(file.isFile) { "Is not a file: $file" }
    return Size(file.length().toULong())
}

private fun getSizeOfDir(dir: File): Size {
    check(dir.isDirectory) { "Is not a directory: $dir" }
    return dir.walkTopDown().filter(File::isFile).map(::getSizeOfFile).reduceOrNull { sum, size -> sum + size } ?: Size.ZERO
}
