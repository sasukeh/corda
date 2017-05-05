package net.corda.quasarhook

import javassist.ClassPool
import java.io.ByteArrayInputStream
import java.lang.instrument.ClassFileTransformer
import java.lang.instrument.Instrumentation
import java.security.ProtectionDomain

class QuasarInstrumentationHookAgent {
    companion object {
        @JvmStatic
        fun premain(arguments: String?, instrumentation: Instrumentation) {
            instrumentation.addTransformer(QuasarInstrumentationHook)
        }
    }

}

object QuasarInstrumentationHook : ClassFileTransformer {
    val classPool = ClassPool.getDefault()
    override fun transform(
            loader: ClassLoader?,
            className: String,
            classBeingRedefined: Class<*>?,
            protectionDomain: ProtectionDomain?,
            classfileBuffer: ByteArray
    ): ByteArray {
        if (className == "co/paralleluniverse/fibers/instrument/QuasarInstrumentor") {
            val clazz = classPool.makeClass(ByteArrayInputStream(classfileBuffer))
            val shouldInstrumentMethod = clazz.methods.first { it.name == "shouldInstrument" }
            shouldInstrumentMethod.insertBefore(
                    "Boolean _shouldInstrument = net.corda.quasarhook.ShouldInstrument.shouldInstrument(className);" +
                            "if (_shouldInstrument != null) return _shouldInstrument.booleanValue();")
            return clazz.toBytecode()
        }
        return classfileBuffer
    }
}