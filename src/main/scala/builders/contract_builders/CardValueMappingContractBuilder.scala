package builders.contract_builders

import org.ergoplatform.appkit._
import utils.TradeInUtils

case class CardValueMappingContractBuilder() extends TokenIssuanceContractBuilder {

    override val script: String = TradeInUtils.CARD_VALUE_MAPPING_SCRIPT

    override def toErgoContract(implicit ctx: BlockchainContext): ErgoContract = {

        ctx.compileContract(
            ConstantsBuilder.empty(),
            script
        )

    }

}

object CardValueMappingContractBuilder {}
