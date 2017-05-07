package net.corda.notarydemo.plugin

import net.corda.core.flows.FlowLogic
import net.corda.core.node.CordaPluginRegistry
import net.corda.flows.NotaryFlow
import net.corda.notarydemo.flows.DummyIssueAndMove

class NotaryDemoPlugin : CordaPluginRegistry() {
    // A list of protocols that are required for this cordapp
    override val requiredFlows: Set<Class<out FlowLogic<*>>> = setOf(
            NotaryFlow.Client::class.java,
            DummyIssueAndMove::class.java
    )
}
