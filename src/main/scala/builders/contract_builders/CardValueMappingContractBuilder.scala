package builders.contract_builders

import org.ergoplatform.appkit._
import utils.TradeInUtils
import special.sigma.GroupElement
import configs.setup_config.TradeInSetupConfig
import configs.report_config.TradeInReportConfig
import utils.TradeInUtils.TRADEIN_REPORT_CONFIG_FILE_PATH

import scala.util.Try

case class CardValueMappingContractBuilder(
    devPKGE: ErgoValue[GroupElement]
) extends TokenIssuanceContractBuilder {

    override val script: String = TradeInUtils.CARD_VALUE_MAPPING_SCRIPT

    override def toErgoContract(implicit ctx: BlockchainContext): ErgoContract = {

        ctx.compileContract(
            ConstantsBuilder.create()
                .item("$DevPKGE", devPKGE.getValue)
                .build(),
            script
        )

    }

}

object CardValueMappingContractBuilder {

    def apply(setupConfig: TradeInSetupConfig): CardValueMappingContractBuilder = {

        val devPKGE: ErgoValue[GroupElement] = ErgoValue.of(Address.createEip3Address(
            setupConfig.node.wallet.index,
            setupConfig.node.networkType,
            SecretString.create(setupConfig.node.wallet.mnemonic),
            SecretString.create(setupConfig.node.wallet.password),
            false
        ).getPublicKeyGE)

        new CardValueMappingContractBuilder(
            devPKGE
        )

    }

    def compile(setupConfig: TradeInSetupConfig)(implicit ctx: BlockchainContext): Try[Unit] = {

        println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: CARD VALUE MAPPING ==========" + Console.RESET)

        // read the report
        val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
        val reportConfig = readReportConfigResult.get

        val contract: ErgoContract = CardValueMappingContractBuilder(setupConfig).toErgoContract
        val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

        // write to the report
        reportConfig.cardValueMappingBoxes.cardValueMappingContract = contractString
        TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

    }

}
