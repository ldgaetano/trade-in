package builders.contract_builders

import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit._
import special.collection.Coll
import special.sigma.SigmaProp
import utils.TradeInUtils
import special.sigma.GroupElement

case class CardValueMappingIssuanceContractBuilder(
                                                    gameLPContractBytes: ErgoValue[Coll[java.lang.Byte]],
                                                    cardValueMappingContractBytes: ErgoValue[Coll[java.lang.Byte]],
                                                    cardSetSize: Long,
                                                    minBoxValue: Long,
                                                    minerFee: Long,
                                                    devPKGE: ErgoValue[GroupElement]
                                                  ) extends TokenIssuanceContractBuilder {

    override val script: String = TradeInUtils.CARD_VALUE_MAPPING_ISSUANCE_SCRIPT

    override def toErgoContract(implicit ctx: BlockchainContext): ErgoContract = {

        ctx.compileContract(
            ConstantsBuilder.create()
              .item("$GameLPContractBytes", gameLPContractBytes)
              .item("$CardValueMappingContractBytes", cardValueMappingContractBytes.getValue())
              .item("$CardSetSize", cardSetSize)
              .item("$MinBoxValue", minBoxValue)
              .item("$MinerFee", minerFee)
              .item("$DevPKGE", devPKGE.getValue())
              .build(),
            script
        )

    }

}

object CardValueMappingIssuanceContractBuilder {

    def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig): CardValueMappingIssuanceContractBuilder = {

        val gameLPContractBytes: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(reportConfig.gameLPBox.gameLPContract).toPropositionBytes)
        val cardValueMappingContract: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(reportConfig.cardValueMappingBox.cardValueMappingContract).toPropositionBytes)

        val cardSetSize: Long = setupConfig.settings.cardValueMappingBoxCreation.cardSetSize

        val minBoxValue: Long = TradeInUtils.calcMinBoxValue()
        val minerFee: Long = setupConfig.settings.minerFeeInNanoERG
        val devPKGE: ErgoValue[GroupElement] = ErgoValue.of(Address.createEip3Address(
            setupConfig.node.wallet.index,
            setupConfig.node.networkType,
            SecretString.create(setupConfig.node.wallet.mnemonic),
            SecretString.create(setupConfig.node.wallet.password),
            false
        ).getPublicKeyGE())

        new CardValueMappingIssuanceContractBuilder(
            gameLPContractBytes,
            cardValueMappingContract,
            cardSetSize,
            minBoxValue,
            minerFee,
            devPKGE
        )

    }

}
