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

case class GameTokenIssuanceContractBuilder(
                                             gameLPIssuanceContractBytes: ErgoValue[Coll[java.lang.Byte]],
                                             gameLPContractBytes: ErgoValue[Coll[java.lang.Byte]],
                                             devPKGE: ErgoValue[GroupElement],
                                           ) extends TokenIssuanceContractBuilder {

    override val script: String = TradeInUtils.GAME_TOKEN_ISSUANCE_SCRIPT

    override def toErgoContract(implicit ctx: BlockchainContext): ErgoContract = {

        ctx.compileContract(
            ConstantsBuilder.create()
              .item("$GameLPIssuanceContractBytes", gameLPIssuanceContractBytes.getValue)
              .item("$GameLPContractBytes", gameLPContractBytes.getValue)
              .item("$DevPKGE", devPKGE.getValue)
              .build(),
            script
        )

    }

}

object GameTokenIssuanceContractBuilder {

    def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig): GameTokenIssuanceContractBuilder = {

        val lpIssuanceContract: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(reportConfig.gameLPIssuanceBox.gameLPIssuanceContract).toPropositionBytes)

        val lpContract: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(reportConfig.gameLPBox.gameLPContract).toPropositionBytes)

        val devPKGE: ErgoValue[GroupElement] = ErgoValue.of(Address.createEip3Address(
            setupConfig.node.wallet.index,
            setupConfig.node.networkType,
            SecretString.create(setupConfig.node.wallet.mnemonic),
            SecretString.create(setupConfig.node.wallet.password),
            false
        ).getPublicKeyGE)

        new GameTokenIssuanceContractBuilder(
            lpIssuanceContract,
            lpContract,
            devPKGE
        )

    }

    def compile(setupConfig: TradeInSetupConfig)(implicit ctx: BlockchainContext): Try[Unit] = {

        println(Console.YELLOW + s"========== ${TradeInUtils.getTimeStamp("UTC")} COMPILING: GAME TOKEN ISSUANCE ==========" + Console.RESET)

        // read the report
        val readReportConfigResult: Try[TradeInReportConfig] = TradeInReportConfig.load(TRADEIN_REPORT_CONFIG_FILE_PATH)
        val reportConfig = readReportConfigResult.get

        val contract: ErgoContract = GameTokenIssuanceContractBuilder(setupConfig, reportConfig).toErgoContract
        val contractString: String = Address.fromErgoTree(contract.getErgoTree, ctx.getNetworkType).toString

        // write to the report
        reportConfig.gameTokenIssuanceBox.gameTokenIssuanceContract = contractString
        TradeInReportConfig.write(TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

    }

}
