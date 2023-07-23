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
  override val changeAddress: Address = Address.fromErgoTree(gameLPBox.getErgoTree, networkType)

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

    // get the dev address: where the game token dev fee will go, does not have to be a PK address, could also be a P2S address
    val devAddress: ErgoValue[Coll[java.lang.Byte]] = ErgoValue.of(Address.create(setupConfig.settings.devAddress).toPropositionBytes)

    // get the dev fee
    val devFeeBigInt: (BigInt, BigInt) = TradeInUtils.decimalToFraction(setupConfig.settings.devFeeInGameTokenPercentage)
    val devFee: ErgoValue[(java.lang.Long, java.lang.Long)] = ErgoValue.pairOf(ErgoValue.of(devFeeBigInt._1.toLong), ErgoValue.of(devFeeBigInt._2.toLong))

    // get the tx operator fee
    val txOperatorFeeBigInt: (BigInt, BigInt) = TradeInUtils.decimalToFraction(setupConfig.settings.txOperatorFeeInGameTokenPercentage)
    val txOperatorFee: ErgoValue[(java.lang.Long, java.lang.Long)] = ErgoValue.pairOf(ErgoValue.of(devFeeBigInt._1.toLong), ErgoValue.of(devFeeBigInt._2.toLong))

    // get protocol parameters
    val emissionInterval: ErgoValue[java.lang.Long] = ErgoValue.of(setupConfig.settings.gameLPBoxCreation.emissionInterval)
    val emissionReductionFactorMultiplier: ErgoValue[java.lang.Long] = ErgoValue.of(setupConfig.settings.gameLPBoxCreation.emissionReductionFactorMultiplier)

    // build the game lp box
    val gameLPBox = new GameLPBoxBuilder(
      lpIssuance.getValue,
      lpBoxContract,
      Eip4TokenBuilder.buildFromErgoBox(reportConfig.gameLPIssuanceBox.gameLPSingletonTokenId, lpIssuance),
      Eip4TokenBuilder.buildFromErgoBox(reportConfig.gameTokenIssuanceBox.gameTokenId, gameTokenIssuance),
      devAddress,
      devFee,
      txOperatorFee,
      emissionInterval,
      emissionReductionFactorMultiplier,
      ErgoValue.of(1L),
      ErgoValue.of(0L),
      ErgoValue.of(0L)
    ).toOutBox(txBuilder.outBoxBuilder())

    // create the tx object
    new GameLPBoxCreationTxBuilder(lpIssuance, gameTokenIssuance, gameLPBox, setupConfig.node.networkType)

  }

}
