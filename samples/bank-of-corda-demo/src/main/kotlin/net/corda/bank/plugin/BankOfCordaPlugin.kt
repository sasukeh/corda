package net.corda.bank.plugin

import net.corda.bank.api.BankOfCordaWebApi
import net.corda.core.flows.FlowLogic
import net.corda.core.node.CordaPluginRegistry
import net.corda.flows.IssuerFlow
import java.util.function.Function

class BankOfCordaPlugin : CordaPluginRegistry() {
    // A list of classes that expose web APIs.
    override val webApis = listOf(Function(::BankOfCordaWebApi))
    // A list of flow that are required for this cordapp
    override val requiredFlows: Set<Class<out FlowLogic<*>>> = setOf(IssuerFlow.IssuanceRequester::class.java)
    override val servicePlugins = listOf(Function(IssuerFlow.Issuer::Service))
}
