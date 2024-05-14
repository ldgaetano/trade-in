package builders.contract_builders

import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit._
import utils.TradeInUtils

case class PlayerProxyContractBuilder(
                                     minTxOperatorFee: ErgoValue[java.lang.Long]
                                     ) extends TokenIssuanceContractBuilder {

    override val script: String = TradeInUtils.PLAYER_PROXY_SCRIPT

    override def toErgoContract(implicit ctx: BlockchainContext): ErgoContract = {

        ctx.compileContract(
            ConstantsBuilder.create()
              .item("$MinTxOperatorFee", minTxOperatorFee.getValue)
              .build(),
            script
        )

    }

}

object PlayerProxyContractBuilder {

    def apply(setupConfig: TradeInSetupConfig): PlayerProxyContractBuilder = {

        val minTxOperatorFee = ErgoValue.of(setupConfig.settings.minTxOperatorFeeInNanoErg)

        new PlayerProxyContractBuilder(
            minTxOperatorFee
        )

    }

}
