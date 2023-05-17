package builders.contract_builders

import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit._
import utils.TradeInUtils

case class PlayerProxyContractBuilder(
                                       minerFee: Long
                                     ) extends TokenIssuanceContractBuilder {

    override val script: String = TradeInUtils.PLAYER_PROXY_SCRIPT

    override def toErgoContract(implicit ctx: BlockchainContext): ErgoContract = {

        ctx.compileContract(
            ConstantsBuilder.create()
              .item("$MinerFee", minerFee)
              .build(),
            script
        )

    }

}

object PlayerProxyContractBuilder {

    def apply(setupConfig: TradeInSetupConfig): PlayerProxyContractBuilder = {

        new PlayerProxyContractBuilder(
            setupConfig.settings.minerFeeInNanoERG
        )

    }

}
