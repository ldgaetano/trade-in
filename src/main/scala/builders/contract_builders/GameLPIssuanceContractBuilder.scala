package builders.contract_builders

import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit._
import special.collection.Coll
import special.sigma.SigmaProp
import utils.TradeInUtils
import special.sigma.GroupElement
import utils.TradeInUtils.TRADEIN_REPORT_CONFIG_FILE_PATH

import scala.util.Try

case class GameLPIssuanceContractBuilder(
                                          gameLPContractBytes: ErgoValue[Coll[java.lang.Byte]],
                                          devPKGE: ErgoValue[GroupElement]
                                        ) extends TokenIssuanceContractBuilder {

    override val script: String = TradeInUtils.GAME_LP_ISSUANCE_SCRIPT

    override def toErgoContract(implicit ctx: BlockchainContext): ErgoContract = {

        ctx.compileContract(
            ConstantsBuilder.create()
              .item("$GameLPContractBytes", gameLPContractBytes.getValue)
              .item("$DevPKGE", devPKGE.getValue)
              .build(),
            script
        )

    }

}

object GameLPIssuanceContractBuilder {

    def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig): GameLPIssuanceContractBuilder = {

        val lpContractBytes: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(reportConfig.gameLPBox.gameLPContract).toPropositionBytes)

        val devPKGE: ErgoValue[GroupElement] = ErgoValue.of(Address.createEip3Address(
            setupConfig.node.wallet.index,
            setupConfig.node.networkType,
            SecretString.create(setupConfig.node.wallet.mnemonic),
            SecretString.create(setupConfig.node.wallet.password),
            false
        ).getPublicKeyGE)

        new GameLPIssuanceContractBuilder(
            lpContractBytes,
            devPKGE
        )

    }

    def compile(setupConfig: TradeInSetupConfig)(implicit ctx: BlockchainContext): Try[Unit] = {

        println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: GAME LP ISSUANCE ==========" + Console.RESET)

        // read the report
        val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
        val reportConfig = readReportConfigResult.get

        val contract: ErgoContract = GameLPIssuanceContractBuilder(setupConfig, reportConfig).toErgoContract
        val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

        // write to the report
        reportConfig.gameLPIssuanceBox.gameLPIssuanceContract = contractString
        TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

    }

}
