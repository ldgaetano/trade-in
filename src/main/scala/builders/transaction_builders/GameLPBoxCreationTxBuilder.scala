package builders.transaction_builders

import builders.box_builders.GameLPBoxBuilder
import builders.contract_builders.GameLPContractBuilder
import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit.impl.Eip4TokenBuilder
import org.ergoplatform.appkit.{Address, BlockchainContext, ErgoContract, ErgoValue, InputBox, NetworkType, OutBox, SecretString, UnsignedTransaction, UnsignedTransactionBuilder}
import special.collection.Coll
import utils.TradeInUtils

case class GameLPBoxCreationTxBuilder(
                                     gameLPIssuanceBox: InputBox,
                                     gameTokenIssuanceBox: InputBox,
                                     gameLPBox: OutBox,
                                     networkType: NetworkType
                                     ) extends TradeInTxBuilder {
  override val txFee: Long = gameTokenIssuanceBox.getValue
  override val changeAddress: Address = Address.fromErgoTree(gameLPBox.getErgoTree, networkType) // There should be no change!

  override def build(implicit txBuilder: UnsignedTransactionBuilder): UnsignedTransaction = {

    txBuilder
      .addInputs(gameLPIssuanceBox, gameTokenIssuanceBox)
      .addOutputs(gameLPBox)
      .fee(txFee)
      .sendChangeTo(changeAddress)
      .build()

  }

}

object GameLPBoxCreationTxBuilder {

  def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig)(implicit ctx: BlockchainContext): GameLPBoxCreationTxBuilder = {

    // tx builder
    val txBuilder: UnsignedTransactionBuilder = ctx.newTxBuilder()

    // get the lp issuance box
    val lpIssuance: InputBox = ctx.getDataSource.getBoxById(reportConfig.gameLPIssuanceBox.boxId, true, true)

    // get the game token issuance box
    val gameTokenIssuance: InputBox = ctx.getDataSource.getBoxById(reportConfig.gameTokenIssuanceBox.boxId, true, true)

    // build the lp box contract
    val lpBoxContract: ErgoContract = GameLPContractBuilder(setupConfig, reportConfig).toErgoContract

    // write to the report
    reportConfig.gameLPBox.gameLPContract = lpBoxContract.getErgoTree.bytesHex
    TradeInReportConfig.write(TradeInUtils.TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

    // get the dev fee
    val devFeeBigInt: (BigInt, BigInt) = TradeInUtils.decimalToFraction(setupConfig.settings.devFeeInGameTokenPercentage)
    val devFee: ErgoValue[(java.lang.Long, java.lang.Long)] = ErgoValue.pairOf(ErgoValue.of(devFeeBigInt._1.toLong), ErgoValue.of(devFeeBigInt._2.toLong))

    // get the trade in fee
    val tradeInFeeBigInt: (BigInt, BigInt) = TradeInUtils.tradeInFeeBigInt
    val tradeInFee: ErgoValue[(java.lang.Long, java.lang.Long)] = ErgoValue.pairOf(ErgoValue.of(tradeInFeeBigInt._1.toLong), ErgoValue.of(tradeInFeeBigInt._2.toLong))

    // get protocol parameters
    val emissionInterval: ErgoValue[java.lang.Long] = ErgoValue.of(setupConfig.settings.gameLPBoxCreation.emissionInterval)
    val emissionReductionFactorMultiplier: ErgoValue[java.lang.Long] = ErgoValue.of(setupConfig.settings.gameLPBoxCreation.emissionReductionFactorMultiplier)

    // build the game lp box
    val gameLPBox = new GameLPBoxBuilder(
      lpIssuance.getValue,
      lpBoxContract,
      Eip4TokenBuilder.buildFromErgoBox(reportConfig.gameLPIssuanceBox.gameLPSingletonTokenId, lpIssuance),
      Eip4TokenBuilder.buildFromErgoBox(reportConfig.gameTokenIssuanceBox.gameTokenId, gameTokenIssuance),
      tradeInFee,
      devFee,
      emissionInterval,
      emissionReductionFactorMultiplier,
      ErgoValue.of(1L), // Initial emission reduction factor
      ErgoValue.of(0L), // Initial card token burn count
      ErgoValue.of(0L)  // Initial card token burn total
    ).toOutBox(txBuilder.outBoxBuilder())

    // create the tx object
    new GameLPBoxCreationTxBuilder(lpIssuance, gameTokenIssuance, gameLPBox, setupConfig.node.networkType)

  }

}
