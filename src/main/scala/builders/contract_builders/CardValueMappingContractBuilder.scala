package builders.contract_builders

import org.ergoplatform.appkit._
import utils.TradeInUtils
import special.sigma.GroupElement
import configs.setup_config.TradeInSetupConfig
import configs.report_config.TradeInReportConfig

case class CardValueMappingContractBuilder(
    devPKGE: ErgoValue[GroupElement]
) extends TokenIssuanceContractBuilder {

    override val script: String = TradeInUtils.CARD_VALUE_MAPPING_SCRIPT

    override def toErgoContract(implicit ctx: BlockchainContext): ErgoContract = {

        ctx.compileContract(
            ConstantsBuilder.create()
                .item("$DevPKGE", devPKGE.getValue())
                .build(),
            script
        )

    }

}

object CardValueMappingContractBuilder {

    def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig): CardValueMappingContractBuilder = {

        val devPKGE: ErgoValue[GroupElement] = ErgoValue.of(Address.createEip3Address(
            setupConfig.node.wallet.index,
            setupConfig.node.networkType,
            SecretString.create(setupConfig.node.wallet.mnemonic),
            SecretString.create(setupConfig.node.wallet.password),
            false
        ).getPublicKeyGE())

        new CardValueMappingContractBuilder(
            devPKGE
        )

    }

}
