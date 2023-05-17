package builders.contract_builders

import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit._
import special.collection.Coll
import special.sigma.SigmaProp
import utils.TradeInUtils

case class CardValueMappingIssuanceContractBuilder(
                                                    cardValueMappingContractBytes: ErgoValue[Coll[java.lang.Byte]],
                                                    safeStorageRentValue: Long,
                                                    devPK: ErgoValue[SigmaProp],
                                                    minerFee: Long
                                                  ) extends TokenIssuanceContractBuilder {

    override val script: String = TradeInUtils.CARD_VALUE_MAPPING_ISSUANCE_SCRIPT

    override def toErgoContract(implicit ctx: BlockchainContext): ErgoContract = {

        ctx.compileContract(
            ConstantsBuilder.create()
              .item("$CardValueMappingContractBytes", cardValueMappingContractBytes.getValue)
              .item("$SafeStorageRentValue", safeStorageRentValue)
              .item("$DevPK", devPK.getValue)
              .item("$MinerFee", minerFee)
              .build(),
            script
        )

    }

}

object CardValueMappingIssuanceContractBuilder {

    def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig): CardValueMappingIssuanceContractBuilder = {

        val cardValueMappingContract: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(reportConfig.cardValueMappingBox.cardValueMappingContract).toPropositionBytes)
        val devPK: ErgoValue[SigmaProp] = ErgoValue.of(Address.fromMnemonic(
            setupConfig.node.networkType,
            SecretString.create(setupConfig.node.wallet.mnemonic),
            SecretString.create(setupConfig.node.wallet.password),
            false
        ).getSigmaBoolean)

        new CardValueMappingIssuanceContractBuilder(
            cardValueMappingContract,
            TradeInUtils.SAFE_STORAGE_RENT_VALUE,
            devPK,
            setupConfig.settings.minerFeeInNanoERG
        )

    }

}
