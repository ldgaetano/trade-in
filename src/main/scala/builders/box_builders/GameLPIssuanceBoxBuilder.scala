package builders.box_builders

import org.ergoplatform.appkit._

case class GameLPIssuanceBoxBuilder(
                               gameLPIssuanceBoxValue: Long,
                               gameLPIssuanceBoxContract: ErgoContract,
                               gameLPSingletonToken: Eip4Token
                               ) extends EIP4TokenIssuanceBoxBuilder {

  override val value: Long = gameLPIssuanceBoxValue
  override val contract: ErgoContract = gameLPIssuanceBoxContract
  override val eip4Token: Eip4Token = gameLPSingletonToken

  override def toOutBox(implicit outBoxBuilder: OutBoxBuilder): OutBox = {
    outBoxBuilder
      .value(value)
      .contract(contract)
      .mintToken(eip4Token)
      .build()
  }

}

object GameLPIssuanceBoxBuilder {

  def apply(input: InputBox)(implicit ctx: BlockchainContext): Option[GameLPIssuanceBoxBuilder] = {

    if (input.getTokens.size == 1) {

      Some(GameLPIssuanceBoxBuilder(
        input.getValue,
        Address.fromErgoTree(input.getErgoTree, ctx.getNetworkType).toErgoContract,
        input.getTokens.get(0).asInstanceOf[Eip4Token],
      ))

    } else {
      None
    }


  }

}
