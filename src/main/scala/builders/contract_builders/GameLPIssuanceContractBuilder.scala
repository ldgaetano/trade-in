package builders.contract_builders

import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit._
import special.collection.Coll
import special.sigma.SigmaProp
import utils.TradeInUtils

case class GameLPIssuanceContractBuilder(
                                          gameLPContractBytes: ErgoValue[Coll[java.lang.Byte]],
                                          safeStorageRentValue: Long,
                                          devPK: ErgoValue[SigmaProp],
                                          devAddress: ErgoValue[Coll[java.lang.Byte]],
                                          minerFee: Long
                                        ) extends TokenIssuanceContractBuilder {

    override val script: String = TradeInUtils.GAME_LP_ISSUANCE_SCRIPT

    override def toErgoContract(implicit ctx: BlockchainContext): ErgoContract = {

        ctx.compileContract(
            ConstantsBuilder.create()
              .item("$GameLPContractBytes", gameLPContractBytes.getValue)
              .item("$SafeStorageRentValue", safeStorageRentValue)
              .item("$DevPK", devPK.getValue)
              .item("$DevAddress", devAddress.getValue)
              .item("$MinerFee", minerFee)
              .build(),
            script
        )

    }

}

object GameLPIssuanceContractBuilder {

    def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig): GameLPIssuanceContractBuilder = {

        val lpContract: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(reportConfig.gameLPBox.gameLPContract).toPropositionBytes)

        val devPK: ErgoValue[SigmaProp] = ErgoValue.of(Address.createEip3Address(
            setupConfig.node.wallet.index,
            setupConfig.node.networkType,
            SecretString.create(setupConfig.node.wallet.mnemonic),
            SecretString.create(setupConfig.node.wallet.password),
            false
        ).getSigmaBoolean)

        val devAddress: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(setupConfig.settings.devAddress).toPropositionBytes)

        new GameLPIssuanceContractBuilder(
            lpContract,
            TradeInUtils.calcSafeStorageRentValue(setupConfig.settings.protocolPeriodInYears),
            devPK,
            devAddress,
            setupConfig.settings.minerFeeInNanoERG
        )

    }

}
