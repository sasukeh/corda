@file:JvmName("ShouldInstrument")
package net.corda.quasarhook

fun shouldInstrument(className: String): Boolean? {
    val whitelistPrefixes = listOf(
            "net/corda/"
    )
    if (whitelistPrefixes.any { className.startsWith(it) }) {
//        println("$className: I'll allow it")
        return true
    }
    return false
}
