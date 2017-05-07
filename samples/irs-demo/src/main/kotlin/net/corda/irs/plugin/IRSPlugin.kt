package net.corda.irs.plugin

import net.corda.core.flows.FlowLogic
import net.corda.core.node.CordaPluginRegistry
import net.corda.irs.api.InterestRateSwapAPI
import net.corda.irs.flows.AutoOfferFlow
import net.corda.irs.flows.FixingFlow
import net.corda.irs.flows.UpdateBusinessDayFlow
import java.util.function.Function

class IRSPlugin : CordaPluginRegistry() {
    override val webApis = listOf(Function(::InterestRateSwapAPI))
    override val staticServeDirs: Map<String, String> = mapOf(
            "irsdemo" to javaClass.classLoader.getResource("irsweb").toExternalForm()
    )
    override val servicePlugins = listOf(Function(FixingFlow::Service))
    override val requiredFlows: Set<Class<out FlowLogic<*>>> = setOf(
            AutoOfferFlow.Requester::class.java,
            UpdateBusinessDayFlow.Broadcast::class.java,
            FixingFlow.FixingRoleDecider::class.java,
            FixingFlow.Floater::class.java)
}
