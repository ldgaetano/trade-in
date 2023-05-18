package builders.transaction_builders

import builders.box_builders.GameTokenIssuanceBoxBuilder
import builders.contract_builders.GameTokenIssuanceContractBuilder
import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit.impl.Eip4TokenBuilder
import org.ergoplatform.appkit.{Address, BlockchainContext, Eip4Token, ErgoContract, InputBox, OutBox, SecretString, UnsignedTransaction, UnsignedTransactionBuilder}
import org.ergoplatform.wallet.mnemonic.Mnemonic
import scorex.crypto.hash
import scorex.crypto.hash.Sha256
import utils.TradeInUtils

import java.io.File
import java.nio.file.Files
import scala.collection.JavaConverters._
import scala.util.Try

case class GameTokenMintingTxBuilder(
                                    devPKBoxes: Array[InputBox],
                                    gameTokenIssuanceBox: OutBox,
                                    changeAddress: Address,
                                    minerFee: Long,
                                    ) extends EIP4TokenMintingTxBuilder {

  override val eip4IssuanceBox: OutBox = gameTokenIssuanceBox
  override val txFee: Long = minerFee

  override def build(implicit txBuilder: UnsignedTransactionBuilder): UnsignedTransaction = {

    txBuilder
      .addInputs(devPKBoxes:_*)
      .addOutputs(eip4IssuanceBox)
      .fee(minerFee)
      .sendChangeTo(changeAddress)
      .build()

  }

}

object GameTokenMintingTxBuilder {

  def apply(setupConfig: TradeInSetupConfig, reportConfig: TradeInReportConfig)(implicit ctx: BlockchainContext): GameTokenMintingTxBuilder = {

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
    val issuanceContract: ErgoContract = GameTokenIssuanceContractBuilder(setupConfig, reportConfig).toErgoContract

    // game token picture content hash
    val picFile: File = new File(TradeInUtils.TRADEIN_GAME_TOKEN_IMG_DIRECTORY_PATH + setupConfig.settings.gameTokenMinting.gameTokenPictureFileName)
    val picBytes: Array[Byte] = Files.readAllBytes(picFile.toPath)
    val picHash: hash.Digest32 = Sha256.hash(picBytes)

    // create the game token
    val gameToken: Eip4Token = Eip4TokenBuilder.buildNftPictureToken(
      inputs(0).getId.toString,
      setupConfig.settings.gameTokenMinting.gameTokenAmount,
      setupConfig.settings.gameTokenMinting.gameTokenName,
      setupConfig.settings.gameTokenMinting.gameTokenDescription,
      setupConfig.settings.gameTokenMinting.gameTokenDecimals,
      picHash,
      setupConfig.settings.gameTokenMinting.gameTokenPictureLink
    )

    // write to the report
    reportConfig.gameTokenIssuanceBox.gameTokenId = gameToken.getId.toString
    TradeInReportConfig.write(TradeInUtils.TRADEIN_REPORT_CONFIG_FILE_PATH, reportConfig)

    // create the game token issuance box
    val issuance: OutBox = GameTokenIssuanceBoxBuilder(issuanceBoxValue, issuanceContract, gameToken).toOutBox(txBuilder.outBoxBuilder())

    // miner fee
    val minerFee: Long = setupConfig.settings.minerFeeInNanoERG

    // create the tx object
    new GameTokenMintingTxBuilder(inputs, issuance, devPK, minerFee)

  }

}
