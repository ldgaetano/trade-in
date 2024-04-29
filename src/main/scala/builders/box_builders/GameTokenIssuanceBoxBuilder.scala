package builders.box_builders

import builders.contract_builders.GameTokenIssuanceContractBuilder
import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit._
import org.ergoplatform.appkit.impl.Eip4TokenBuilder

case class GameTokenIssuanceBoxBuilder(
                               gameTokenIssuanceBoxValue: Long,
                               gameTokenIssuanceContract: ErgoContract,
                               gameToken: Eip4Token
                               ) extends EIP4TokenIssuanceBoxBuilder {

  override val value: Long = gameTokenIssuanceBoxValue
  override val contract: ErgoContract = gameTokenIssuanceContract
  override val eip4Token: Eip4Token = gameToken

  override def toOutBox(implicit outBoxBuilder: OutBoxBuilder): OutBox = {
    outBoxBuilder
      .value(value)
      .contract(contract)
      .mintToken(eip4Token)
      .build()
  }

}

object GameTokenIssuanceBoxBuilder {

  // Take an existing input box that was created as a GameTokenIssuanceBox output,
  // and convert it to an instance of the GameTokenIssuanceBox.
  def apply(input: InputBox)(implicit ctx: BlockchainContext): Option[GameTokenIssuanceBoxBuilder] = {

      if (input.getTokens.size() == 1) {

      Some(GameTokenIssuanceBoxBuilder(
        input.getValue,
        Address.fromErgoTree(input.getErgoTree, ctx.getNetworkType).toErgoContract,
        input.getTokens.get(0).asInstanceOf[Eip4Token],
      ))

    } else {
      None
    }

  }

}
