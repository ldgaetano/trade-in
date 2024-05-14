package builders.contract_builders

import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit._
import utils.TradeInUtils
import utils.TradeInUtils.TRADEIN_REPORT_CONFIG_FILE_PATH

import scala.util.Try

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

    def compile(setupConfig: TradeInSetupConfig)(implicit ctx: BlockchainContext): Try[Unit] = {

        println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: PLAYER PROXY ==========" + Console.RESET)

        val contract: ErgoContract = PlayerProxyContractBuilder(setupConfig).toErgoContract
        val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

        // read the report
        val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
        val reportConfig = readReportConfigResult.get

        // write to the report
        reportConfig.playerProxyBox.playerProxyContract = contractString
        TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

    }

}
