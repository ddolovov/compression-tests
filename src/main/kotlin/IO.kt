package ddol.compression.tests

import java.io.File

fun File.ensureIsDirectory(message: (File) -> String): File {
    check(isDirectory) { message(this) }
    return this
}

fun File.createAsEmptyDir(): File {
    if (exists()) deleteRecursively()
    mkdirs()
    return this
}
