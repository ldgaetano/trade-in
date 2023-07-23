package builders.contract_builders

import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import utils.TradeInUtils

import org.ergoplatform.appkit._
import sigmastate.Values
import special.collection.Coll
import special.sigma.SigmaProp
import sigmastate.Values.SigmaPropValue

case class GameLPContractBuilder(
                                  cardValueMappingContractBytes: ErgoValue[Coll[java.lang.Byte]],
                                  playerProxyContractBytes: ErgoValue[Coll[java.lang.Byte]],
                                  devPK: ErgoValue[SigmaProp],
                                  minBoxValue: Long,
                                  setCreationMultiSigThreshold: Int,
                                  setCreationMultiSigAddresses: ErgoValue[Coll[SigmaProp]]
                                ) extends TradeInContractBuilder {

    override val script: String = TradeInUtils.GAME_LP_SCRIPT

    override def toErgoContract(implicit ctx: BlockchainContext): ErgoContract = {

        ctx.compileContract(
            ConstantsBuilder.create()
              .item("$CardValueMappingContractBytes", cardValueMappingContractBytes.getValue)
              .item("$PlayerProxyContractBytes", playerProxyContractBytes.getValue)
              .item("$DevPK", devPK)
              .item("$MinBoxValue", minBoxValue)
              .item("$SetCreationMultiSigThreshold", setCreationMultiSigThreshold)
              .item("$SetCreationMultiSigAddresses", setCreationMultiSigAddresses.getValue)
              .build(),
            script
        )

    }

}

object GameLPContractBuilder {

    def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig): GameLPContractBuilder = {

        val cardValueMappingContract: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(reportConfig.cardValueMappingBox.cardValueMappingContract).toPropositionBytes)

        val playerProxyContract: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(reportConfig.playerProxyBox.playerProxyContract).toPropositionBytes)

        val devPK: ErgoValue[SigmaProp] = ErgoValue.of(Address.createEip3Address(
            setupConfig.node.wallet.index,
            setupConfig.node.networkType,
            SecretString.create(setupConfig.node.wallet.mnemonic),
            SecretString.create(setupConfig.node.wallet.password),
            false
        ).getSigmaBoolean)

        val multisigThreshold: Int = setupConfig.settings.setCreationMultiSig.threshold
        val multisigStrings: Array[String] = setupConfig.settings.setCreationMultiSig.addresses
        val multisigProps: Array[SigmaProp] = multisigStrings.map(s => ErgoValue.of(Address.create(s).getSigmaBoolean).getValue)
        val multisigAddresses: ErgoValue[Coll[SigmaProp]] = ErgoValue.of(multisigProps, ErgoType.sigmaPropType())

        new GameLPContractBuilder(
            cardValueMappingContract,
            playerProxyContract,
            devPK,
            TradeInUtils.calcMinBoxValue(),
            multisigThreshold,
            multisigAddresses
        )

    }

}
