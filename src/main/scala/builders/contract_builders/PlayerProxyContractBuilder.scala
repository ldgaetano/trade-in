package builders.contract_builders

import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit._
import utils.TradeInUtils

case class PlayerProxyContractBuilder() extends TokenIssuanceContractBuilder {

    override val script: String = TradeInUtils.PLAYER_PROXY_SCRIPT

    override def toErgoContract(implicit ctx: BlockchainContext): ErgoContract = {

        ctx.compileContract(
            ConstantsBuilder.empty(),
            script
        )

    }

}

object PlayerProxyContractBuilder {}
