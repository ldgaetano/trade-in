package builders.box_builders

import builders.contract_builders.GameTokenIssuanceContractBuilder
import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit._
import org.ergoplatform.appkit.impl.Eip4TokenBuilder

case class GameTokenIssuanceBoxBuilder(
                               gameTokenBoxValue: Long,
                               gameTokenBoxContract: ErgoContract,
                               gameToken: Eip4Token
                               ) extends EIP4TokenIssuanceBoxBuilder {

  override val value: Long = gameTokenBoxValue
  override val contract: ErgoContract = gameTokenBoxContract
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

  def apply(input: InputBox, networkType: NetworkType): GameLPIssuanceBoxBuilder = {

    GameLPIssuanceBoxBuilder(
      input.getValue,
      Address.fromErgoTree(input.getErgoTree, networkType).toErgoContract,
      input.getTokens.get(0).asInstanceOf[Eip4Token],
    )

  }


}
