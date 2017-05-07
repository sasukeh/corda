package net.corda.traderdemo.plugin

import net.corda.core.flows.FlowLogic
import net.corda.core.node.CordaPluginRegistry
import net.corda.traderdemo.flow.BuyerFlow
import net.corda.traderdemo.flow.SellerFlow
import java.util.function.Function

class TraderDemoPlugin : CordaPluginRegistry() {
    // A list of Flows that are required for this cordapp
    override val requiredFlows: Set<Class<out FlowLogic<*>>> = setOf(SellerFlow::class.java)
    override val servicePlugins = listOf(Function(BuyerFlow::Service))
}
