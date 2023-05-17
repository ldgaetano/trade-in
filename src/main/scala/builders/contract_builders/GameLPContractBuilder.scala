package builders.contract_builders

import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit._
import special.collection.Coll
import utils.TradeInUtils

case class GameLPContractBuilder(
                                  cardValueMappingContractBytes: ErgoValue[Coll[java.lang.Byte]],
                                  playerProxyContractBytes: ErgoValue[Coll[java.lang.Byte]],
                                  devFee: Long,
                                  txOperatorFee: Long,
                                  minBoxValue: Long
                                ) extends TradeInContractBuilder {

    override val script: String = TradeInUtils.GAME_LP_SCRIPT

    override def toErgoContract(implicit ctx: BlockchainContext): ErgoContract = {

        ctx.compileContract(
            ConstantsBuilder.create()
              .item("$CardValueMappingContractBytes", cardValueMappingContractBytes.getValue)
              .item("$PlayerProxyContractBytes", playerProxyContractBytes.getValue)
              .item("$DevFee", devFee)
              .item("$TxOperatorFee", txOperatorFee)
              .item("$MinBoxValue", minBoxValue)
              .build(),
            script
        )

    }

}

object GameLPContractBuilder {

    def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig): GameLPContractBuilder = {

        val cardValueMappingContract: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(reportConfig.cardValueMappingBox.cardValueMappingContract).toPropositionBytes)
        val playerProxyContract: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(reportConfig.playerProxyBox.playerProxyContract).toPropositionBytes)
        new GameLPContractBuilder(
            cardValueMappingContract,
            playerProxyContract,
            setupConfig.settings.devFeeInGameToken,
            setupConfig.settings.txOperatorFeeInGameToken,
            TradeInUtils.MIN_BOX_VALUE
        )

    }

}
