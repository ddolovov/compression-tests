package ddol.compression.tests.cli

import java.io.File

enum class Tool(val alias: String, val fileExtension: String) {
    ZIP("zip", "zip"),
    UNZIP("unzip", "zip"),
    TAR("tar", "tar"),
    GZIP("gzip", "gz"),
    GUNZIP("gunzip", "gz"),
    BZIP2("bzip2", "bz2"),
    BUNZIP2("bunzip2", "bz2"),
    XZ("xz", "xz"),
    UNXZ("unxz", "xz"),
    SEVEN_ZIP("7z", "7z"),
    ZSTD("zstd", "zst"),
    UNZSTD("unzstd", "zst"),
    ;

    fun locate(): File? {
        val result = CliCommandRunner(timeoutSeconds = null).runCommand("which", alias)
        if (result.exitCode != 0) return null
        return result.stdout.lines().firstOrNull()?.trim()?.takeIf(String::isNotEmpty)?.let(::File)?.takeIf(File::isFile)
    }
}
