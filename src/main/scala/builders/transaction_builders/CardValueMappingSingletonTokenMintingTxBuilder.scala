package builders.transaction_builders

import builders.box_builders.CardValueMappingIssuanceBoxBuilder
import builders.contract_builders.{CardValueMappingContractBuilder, GameLPIssuanceContractBuilder}
import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit.impl.Eip4TokenBuilder
import org.ergoplatform.appkit._
import utils.TradeInUtils

import scala.collection.JavaConverters._

case class CardValueMappingSingletonTokenMintingTxBuilder(
                                      devPKBoxes: Array[InputBox],
                                      cardValueMappingIssuance: OutBox,
                                      devPKAddress: Address,
                                      minerFee: Long,
                                    ) extends EIP4TokenMintingTxBuilder {

  override val eip4IssuanceBox: OutBox = cardValueMappingIssuance
  override val txFee: Long = minerFee
  override val changeAddress: Address = devPKAddress

  override def build(implicit txBuilder: UnsignedTransactionBuilder): UnsignedTransaction = {

    txBuilder
      .addInputs(devPKBoxes:_*)
      .addOutputs(eip4IssuanceBox)
      .fee(txFee)
      .sendChangeTo(changeAddress)
      .build()

  }

}

object CardValueMappingSingletonTokenMintingTxBuilder {

  def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig)(implicit ctx: BlockchainContext): CardValueMappingSingletonTokenMintingTxBuilder = {

    // tx builder
    val txBuilder: UnsignedTransactionBuilder = ctx.newTxBuilder()

    // dev pk
    val devPK: Address = Address.createEip3Address(
      setupConfig.node.wallet.index,
      setupConfig.node.networkType,
      SecretString.create(setupConfig.node.wallet.mnemonic),
      SecretString.create(setupConfig.node.wallet.password),
      false
    )

    // get the requisite input boxes required
    val inputs: Array[InputBox] = ctx.getDataSource.getUnspentBoxesFor(devPK, 0, 100).asScala.toArray

    // card value mapping issuance box value
    val issuanceBoxValue: Long = TradeInUtils.calcSafeStorageRentValue(setupConfig.settings.protocolPeriodInYears) + setupConfig.settings.minerFeeInNanoERG

    // create the card value mapping issuance box contract
    val issuanceContract: ErgoContract = CardValueMappingContractBuilder().toErgoContract

    // create the singleton token
    val cardValueMappingToken: Eip4Token = Eip4TokenBuilder.buildNftPictureToken(
      inputs(0).getId.toString,
      1,
      "Trade-In_" + setupConfig.settings.gameTokenMinting.gameTokenName + "_Card-Value-Mapping_Singleton_Token",
      "Trade-In protocol card-value-mapping singleton token for " + setupConfig.settings.gameTokenMinting.gameTokenName,
      0,
      Array(),
      ""
    )

    // write to the report
    reportConfig.cardValueMappingIssuanceBox.cardValueMappingSingletonTokenId = cardValueMappingToken.getId.toString
    TradeInReportConfig.write(TradeInUtils.TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

    // create the card value mapping issuance box
    val issuance: OutBox = CardValueMappingIssuanceBoxBuilder(issuanceBoxValue, issuanceContract, cardValueMappingToken).toOutBox(txBuilder.outBoxBuilder())

    // miner fee
    val minerFee: Long = setupConfig.settings.minerFeeInNanoERG

    // create the tx object
    new CardValueMappingSingletonTokenMintingTxBuilder(inputs, issuance, devPK, minerFee)

  }

}
