package builders.transaction_builders

import builders.box_builders.GameLPIssuanceBoxBuilder
import builders.contract_builders.{GameLPIssuanceContractBuilder, GameTokenIssuanceContractBuilder}
import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit.impl.Eip4TokenBuilder
import org.ergoplatform.appkit.{Address, BlockchainContext, Eip4Token, ErgoContract, InputBox, OutBox, SecretString, UnsignedTransaction, UnsignedTransactionBuilder}
import scorex.crypto.hash
import scorex.crypto.hash.Sha256
import utils.TradeInUtils

import java.io.File
import java.nio.file.Files
import scala.collection.JavaConverters._

case class GameLPSingletonTokenMintingTxBuilder(
                                      devPKBoxes: Array[InputBox],
                                      gameLPIssuance: OutBox,
                                      devPKAddress: Address,
                                      minerFee: Long,
                                    ) extends EIP4TokenMintingTxBuilder {

  override val eip4IssuanceBox: OutBox = gameLPIssuance
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

object GameLPSingletonTokenMintingTxBuilder {

  def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig)(implicit ctx: BlockchainContext): GameLPSingletonTokenMintingTxBuilder = {

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

    // game token issuance box value
    val issuanceBoxValue: Long = setupConfig.settings.minerFeeInNanoERG

    // create the game token issuance box contract
    val issuanceContract: ErgoContract = GameLPIssuanceContractBuilder(setupConfig, reportConfig).toErgoContract

    // game token properties
    val id = inputs(0).getId.toString
    val amount = 1L
    val name = "Trade-In " + setupConfig.settings.gameTokenMinting.gameTokenName + " LP Singleton Token"
    val description = "Trade-In protocol game liquidity pool singleton token for " + setupConfig.settings.gameTokenMinting.gameTokenName
    val decimals = 0

    // create the game token
    val lpToken: Eip4Token = new Eip4Token(
      id,
      amount,
      name,
      description,
      decimals,
      null,
      null,
      null
    )

    // write to the report
    reportConfig.gameLPIssuanceBox.gameLPSingletonTokenId = lpToken.getId.toString
    TradeInReportConfig.write(TradeInUtils.TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

    // create the game token issuance box
    val issuance: OutBox = GameLPIssuanceBoxBuilder(issuanceBoxValue, issuanceContract, lpToken).toOutBox(txBuilder.outBoxBuilder())

    // miner fee
    val minerFee: Long = setupConfig.settings.minerFeeInNanoERG

    // create the tx object
    new GameLPSingletonTokenMintingTxBuilder(inputs, issuance, devPK, minerFee)

  }

}
