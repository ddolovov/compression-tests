@file:Suppress("MemberVisibilityCanBePrivate")

package ddol.compression.tests

import java.io.File

class Payload(val name: String, val path: File) {
    val size: Size by lazy { getSizeOf(path) }

    fun copyTo(otherDir: File): Payload {
        val newPath = otherDir.resolve(path.name)
        path.copyRecursively(newPath)
        return Payload(name, newPath)
    }
}
