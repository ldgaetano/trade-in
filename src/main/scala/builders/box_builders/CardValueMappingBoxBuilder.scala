package builders.box_builders

import org.ergoplatform.appkit.impl.Eip4TokenBuilder
import org.ergoplatform.appkit.{Address, BlockchainContext, Eip4Token, ErgoContract, ErgoValue, InputBox, OutBox, OutBoxBuilder}
import special.collection.Coll

case class CardValueMappingBoxBuilder(
                             cardValueMappingBoxValue: Long,
                             cardValueMappingBoxContract: ErgoContract,
                             cardValueMappingToken: Eip4Token,
                             gameTokenId: ErgoValue[Coll[java.lang.Byte]],
                             cardSetCollectionTokenId: ErgoValue[Coll[java.lang.Byte]],
                             cardTokenId: ErgoValue[Coll[java.lang.Byte]],
                             cardValue: ErgoValue[java.lang.Long]
                           ) extends TradeInBoxBuilder {
  override val value: Long = cardValueMappingBoxValue
  override val contract: ErgoContract = cardValueMappingBoxContract

  override def toOutBox(implicit outBoxBuilder: OutBoxBuilder): OutBox = {

    outBoxBuilder
      .value(value)
      .contract(contract)
      .tokens(
        cardValueMappingToken
      )
      .registers(
        gameTokenId,
        cardSetCollectionTokenId,
        cardTokenId,
        cardValue
      )
      .build()

  }

}

object CardValueMappingBoxBuilder {

  def apply(input: InputBox)(implicit ctx: BlockchainContext): Option[CardValueMappingBoxBuilder] = {

    if ((input.getTokens.size == 1) & (input.getRegisters.size() == 4)) {

      Some(
        new CardValueMappingBoxBuilder(
          input.getValue,
          Address.fromErgoTree(input.getErgoTree, ctx.getNetworkType).toErgoContract,
          Eip4TokenBuilder.buildFromErgoBox(input.getTokens.get(0).getId.toString, input),
          input.getRegisters.get(0).asInstanceOf[ErgoValue[Coll[java.lang.Byte]]],
          input.getRegisters.get(1).asInstanceOf[ErgoValue[Coll[java.lang.Byte]]],
          input.getRegisters.get(2).asInstanceOf[ErgoValue[Coll[java.lang.Byte]]],
          input.getRegisters.get(3).asInstanceOf[ErgoValue[java.lang.Long]]
        )
      )

    } else {

      None

    }

  }

}
