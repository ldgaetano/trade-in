package builders.box_builders
import org.ergoplatform.appkit.{Address, BlockchainContext, Eip4Token, ErgoContract, ErgoType, ErgoValue, InputBox, NetworkType, OutBox, OutBoxBuilder}
import special.collection.Coll

case class CardValueMappingBoxBuilder(
                             cardValueMappingBoxValue: Long,
                             cardValueMappingBoxContract: ErgoContract,
                             cardValueMappingSingletonToken: Eip4Token,
                             cardSetCollectionTokenId: Array[Byte],
                             gameTokenId: Array[Byte],
                             totalCards: Long,
                             totalDataBuckets: Byte,
                             cardValueMapping: Map[Array[Byte], Long]
                           ) extends TradeInBoxBuilder {
  override val value: Long = cardValueMappingBoxValue
  override val contract: ErgoContract = cardValueMappingBoxContract
  val parameters: (Array[Byte], (Array[Byte], (Long, (Byte, Byte)))) = computeParameters()

  override def toOutBox(implicit outBoxBuilder: OutBoxBuilder): OutBox = {

    outBoxBuilder
      .value(value)
      .contract(contract)
      .tokens(
        cardValueMappingSingletonToken
      )
      .registers(
        computerErgoValueParameters(),

      )
      .build()

  }

  private def computerErgoValueParameters(): ErgoValue[(Coll[Byte], (Coll[Byte], (Long, (Byte, Byte))))] = {

    ErgoValue.pairOf(ErgoValue.of(parameters._1),
      ErgoValue.pairOf(ErgoValue.of(parameters._2._1),
        ErgoValue.pairOf(ErgoValue.of(parameters._2._2._1),
          ErgoValue.pairOf(ErgoValue.of(parameters._2._2._2._1), ErgoValue.of(parameters._2._2._2._2)))))

  }

  private def computeParameters(): (Array[Byte], (Array[Byte], (Long, (Byte, Byte)))) = {

    val dataBucketSize: java.lang.Byte = math.ceil(totalCards / totalDataBuckets).toByte

    (cardSetCollectionTokenId, (gameTokenId, (totalCards, (totalDataBuckets, dataBucketSize))))

  }

  private def computeDataBuckets(): List[ErgoValue[_]] = {



  }

}

object CardValueMappingBoxBuilder {

  def apply(input: InputBox)(implicit ctx: BlockchainContext): Option[CardValueMappingBoxBuilder] = {

    if ((input.getTokens.size == 2) & (input.getRegisters.size() == 6)) {

      Some(
        GameLPBoxBuilder(
          input.getValue,
          Address.fromErgoTree(input.getErgoTree, ctx.getNetworkType).toErgoContract,
          input.getTokens.get(0).asInstanceOf[Eip4Token],
          input.getTokens.get(1).asInstanceOf[Eip4Token],
          input.getRegisters.get(0).asInstanceOf[ErgoValue[Coll[java.lang.Byte]]],
          input.getRegisters.get(1).asInstanceOf[ErgoValue[java.lang.Long]],
          input.getRegisters.get(2).asInstanceOf[ErgoValue[java.lang.Long]],
          input.getRegisters.get(3).asInstanceOf[ErgoValue[java.lang.Long]],
          input.getRegisters.get(4).asInstanceOf[ErgoValue[java.lang.Long]],
          input.getRegisters.get(5).asInstanceOf[ErgoValue[java.lang.Long]]
        )
      )

    } else {

      None

    }

  }

}
