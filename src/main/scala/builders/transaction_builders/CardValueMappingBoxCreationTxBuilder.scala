package builders.transaction_builders

import builders.box_builders.{CardValueMappingBoxBuilder, GameLPBoxBuilder}
import builders.contract_builders.{CardValueMappingContractBuilder, GameLPContractBuilder}
import configs.mapping_config.TradeInMappingConfig
import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit.impl.Eip4TokenBuilder
import org.ergoplatform.appkit._
import sigmastate.Values.ErgoTree
import sigmastate.serialization.ErgoTreeSerializer
import special.collection.Coll
import utils.TradeInUtils

case class CardValueMappingBoxCreationTxBuilder(
                                     gameLPBox: InputBox,
                                     cardValueMappingIssuanceBox: InputBox,
                                     gameLPBoxOut: OutBox,
                                     cardValueMappingBoxes: Array[OutBox],
                                     minerFee: Long,
                                     networkType: NetworkType
                                     ) extends TradeInTxBuilder {
  override val txFee: Long = minerFee
  override val changeAddress: Address = Address.fromErgoTree(gameLPBox.getErgoTree, networkType) // There should be no change!

  override def build(implicit txBuilder: UnsignedTransactionBuilder): UnsignedTransaction = {

    val outputs = Array(gameLPBoxOut) ++ cardValueMappingBoxes

    txBuilder
      .addInputs(gameLPBox, cardValueMappingIssuanceBox)
      .addOutputs(outputs:_*)
      .fee(txFee)
      .sendChangeTo(changeAddress)
      .build()

  }

}

object CardValueMappingBoxCreationTxBuilder {

  def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig, mapConfig: TradeInMappingConfig)(implicit ctx: BlockchainContext): CardValueMappingBoxCreationTxBuilder = {

    // tx builder
    val txBuilder: UnsignedTransactionBuilder = ctx.newTxBuilder()

    // get the game lp box
    val gameLPBoxes = ctx.getDataSource.getUnspentBoxesFor(Address.fromErgoTree(ErgoTreeSerializer.DefaultSerializer.deserializeErgoTree(reportConfig.gameLPBox.gameLPContract.getBytes), setupConfig.node.networkType), 0, 1)
    val gameLPBox: InputBox = gameLPBoxes.get(0)

    // get the game token issuance box
    val cardValueMappingIssuanceBox: InputBox = ctx.getDataSource.getBoxById(reportConfig.cardValueMappingIssuanceBox.boxId, true, true)

    // build the card value mapping box contract
    val mappingBoxContract: ErgoContract = CardValueMappingContractBuilder(setupConfig).toErgoContract

    // build the card value mapping boxes
    val minValue = setupConfig.settings.minerFeeInNanoERG
    val cardSetSize = setupConfig.settings.cardValueMappingBoxCreation.cardSetSize.toInt
    var cardValueMappingBoxes: Array[OutBox] = Array()
    for (i <- 1 to cardSetSize) {

      val cardValueMappingBox = new CardValueMappingBoxBuilder(
        minValue,
        mappingBoxContract,
        Eip4TokenBuilder.buildFromErgoBox(reportConfig.cardValueMappingIssuanceBox.cardValueMappingTokenId, cardValueMappingIssuanceBox),
        ErgoValue.of(reportConfig.gameTokenIssuanceBox.gameTokenId.getBytes),
        ErgoValue.of(setupConfig.settings.cardValueMappingBoxCreation.cardSetCollectionTokenId.getBytes),
        ErgoValue.of(mapConfig.cardValueMapping(i).cardTokenId.getBytes),
        ErgoValue.of(mapConfig.cardValueMapping(i).gameTokenValue)
      ).toOutBox(txBuilder.outBoxBuilder())

      cardValueMappingBoxes = cardValueMappingBoxes ++ Array(cardValueMappingBox)

      // update the report object
      reportConfig.cardValueMappingBoxes(i).cardValueMappingContract = mappingBoxContract.getErgoTree.bytesHex

    }
    // write to the report
    TradeInReportConfig.write(TradeInUtils.TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)


    // get the game lp box output
    val gameLPBoxOut = GameLPBoxBuilder(gameLPBox).get.toOutBox(txBuilder.outBoxBuilder())

    // create the tx object
    new CardValueMappingBoxCreationTxBuilder(
      gameLPBox,
      cardValueMappingIssuanceBox,
      gameLPBoxOut,
      cardValueMappingBoxes,
      minValue,
      setupConfig.node.networkType
    )

  }

}
