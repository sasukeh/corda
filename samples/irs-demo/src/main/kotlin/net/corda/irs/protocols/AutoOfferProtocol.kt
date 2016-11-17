package net.corda.irs.protocols

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.DealState
import net.corda.core.crypto.Party
import net.corda.core.node.CordaPluginRegistry
import net.corda.core.node.PluginServiceHub
import net.corda.core.protocols.ProtocolLogic
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import net.corda.node.services.api.ServiceHubInternal
import net.corda.protocols.TwoPartyDealProtocol
import net.corda.protocols.TwoPartyDealProtocol.Acceptor
import net.corda.protocols.TwoPartyDealProtocol.AutoOffer
import net.corda.protocols.TwoPartyDealProtocol.Instigator

/**
 * This whole class is really part of a demo just to initiate the agreement of a deal with a simple
 * API call from a single party without bi-directional access to the database of offers etc.
 *
 * In the "real world", we'd probably have the offers sitting in the platform prior to the agreement step
 * or the protocol would have to reach out to external systems (or users) to verify the deals.
 */
object AutoOfferProtocol {

    class Plugin : CordaPluginRegistry() {
        override val servicePlugins: List<Class<*>> = listOf(Service::class.java)
    }


    class Service(services: PluginServiceHub) : SingletonSerializeAsToken() {

        object DEALING : ProgressTracker.Step("Starting the deal protocol") {
            override fun childProgressTracker(): ProgressTracker = TwoPartyDealProtocol.Secondary.tracker()
        }

        init {
            services.registerProtocolInitiator(Instigator::class) { Acceptor(it) }
        }
    }

    class Requester(val dealToBeOffered: DealState) : ProtocolLogic<SignedTransaction>() {

        companion object {
            object RECEIVED : ProgressTracker.Step("Received API call")
            object DEALING : ProgressTracker.Step("Starting the deal protocol") {
                override fun childProgressTracker(): ProgressTracker = TwoPartyDealProtocol.Primary.tracker()
            }

            // We vend a progress tracker that already knows there's going to be a TwoPartyTradingProtocol involved at some
            // point: by setting up the tracker in advance, the user can see what's coming in more detail, instead of being
            // surprised when it appears as a new set of tasks below the current one.
            fun tracker() = ProgressTracker(RECEIVED, DEALING)
        }

        override val progressTracker = tracker()

        init {
            progressTracker.currentStep = RECEIVED
        }

        @Suspendable
        override fun call(): SignedTransaction {
            require(serviceHub.networkMapCache.notaryNodes.isNotEmpty()) { "No notary nodes registered" }
            val notary = serviceHub.networkMapCache.notaryNodes.first().notaryIdentity
            // need to pick which ever party is not us
            val otherParty = notUs(dealToBeOffered.parties).single()
            progressTracker.currentStep = DEALING
            val myKey = serviceHub.legalIdentityKey
            val instigator = Instigator(
                    otherParty,
                    AutoOffer(notary, dealToBeOffered),
                    myKey,
                    progressTracker.getChildProgressTracker(DEALING)!!
            )
            val stx = subProtocol(instigator)
            return stx
        }

        private fun notUs(parties: List<Party>): List<Party> {
            val notUsParties: MutableList<Party> = arrayListOf()
            for (party in parties) {
                if (serviceHub.myInfo.legalIdentity != party) {
                    notUsParties.add(party)
                }
            }
            return notUsParties
        }

    }
}