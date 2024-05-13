package builders.transaction_builders

import builders.box_builders.CardValueMappingIssuanceBoxBuilder
import builders.contract_builders.{CardValueMappingContractBuilder, GameLPIssuanceContractBuilder}
import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit.impl.Eip4TokenBuilder
import org.ergoplatform.appkit._
import special.sigma.GroupElement
import utils.TradeInUtils

import scala.collection.JavaConverters._

case class CardValueMappingTokenMintingTxBuilder(
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

object CardValueMappingTokenMintingTxBuilder {

  def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig)(implicit ctx: BlockchainContext): CardValueMappingTokenMintingTxBuilder = {

    // tx builder
    val txBuilder: UnsignedTransactionBuilder = ctx.newTxBuilder()

    // dev pk
    val devPK = Address.createEip3Address(
      setupConfig.node.wallet.index,
      setupConfig.node.networkType,
      SecretString.create(setupConfig.node.wallet.mnemonic),
      SecretString.create(setupConfig.node.wallet.password),
      false
    )

    // dev pk ge
    val devPKGE: ErgoValue[GroupElement] = ErgoValue.of(devPK.getPublicKeyGE)

    // get the requisite input boxes required
    val inputs: Array[InputBox] = ctx.getDataSource.getUnspentBoxesFor(devPK, 0, 100).asScala.toArray

    // card value mapping issuance box value
    val issuanceBoxValue: Long = setupConfig.settings.minerFeeInNanoERG

    // create the card value mapping issuance box contract
    val issuanceContract: ErgoContract = CardValueMappingContractBuilder(
      devPKGE
    ).toErgoContract

    // create the token
    val cardSetSize: Long = setupConfig.settings.cardValueMappingBoxCreation.cardSetSize
    val cardSetId: String = setupConfig.settings.cardValueMappingBoxCreation.cardSetCollectionTokenId

    val id = inputs(0).getId.toString
    val size = cardSetSize
    val name = "Trade-In " + setupConfig.settings.gameTokenMinting.gameTokenName + " Card-Value-Mapping Token"
    val description = "Trade-In protocol card-value-mapping tokens for card set: " + cardSetId + "of game: " + setupConfig.settings.gameTokenMinting.gameTokenName
    val decimals = 0

    val cardValueMappingToken: Eip4Token = new Eip4Token(
      id,
      size,
      name,
      description,
      decimals,
      null,
      null,
      null
    )

    // write to the report
    reportConfig.cardValueMappingIssuanceBox.cardValueMappingTokenId = cardValueMappingToken.getId.toString
    TradeInReportConfig.write(TradeInUtils.TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

    // create the card value mapping issuance box
    val issuance: OutBox = CardValueMappingIssuanceBoxBuilder(issuanceBoxValue, issuanceContract, cardValueMappingToken).toOutBox(txBuilder.outBoxBuilder())

    // miner fee
    val minerFee: Long = setupConfig.settings.minerFeeInNanoERG

    // create the tx object
    new CardValueMappingTokenMintingTxBuilder(inputs, issuance, devPK, minerFee)

  }

}
