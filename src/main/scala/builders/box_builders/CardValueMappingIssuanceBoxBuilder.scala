package builders.box_builders

import org.ergoplatform.appkit._

case class CardValueMappingIssuanceBoxBuilder(
                               cardValueMappingIssuanceBoxValue: Long,
                               cardValueMappingIssuanceBoxContract: ErgoContract,
                               cardValueMappingSingletonToken: Eip4Token
                               ) extends EIP4TokenIssuanceBoxBuilder {

  override val value: Long = cardValueMappingIssuanceBoxValue
  override val contract: ErgoContract = cardValueMappingIssuanceBoxContract
  override val eip4Token: Eip4Token = cardValueMappingSingletonToken

  override def toOutBox(implicit outBoxBuilder: OutBoxBuilder): OutBox = {
    outBoxBuilder
      .value(value)
      .contract(contract)
      .mintToken(eip4Token)
      .build()
  }

}

object CardValueMappingIssuanceBoxBuilder {

  def apply(input: InputBox)(implicit ctx: BlockchainContext): Option[CardValueMappingIssuanceBoxBuilder] = {

    if (input.getTokens.size == 1) {

      Some(CardValueMappingIssuanceBoxBuilder(
        input.getValue,
        Address.fromErgoTree(input.getErgoTree, ctx.getNetworkType).toErgoContract,
        input.getTokens.get(0).asInstanceOf[Eip4Token],
      ))

    } else {
      None
    }


  }

}
