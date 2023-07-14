package builders.box_builders

import org.ergoplatform.appkit.impl.Eip4TokenBuilder
import org.ergoplatform.appkit.{Address, BlockchainContext, Eip4Token, ErgoContract, ErgoToken, ErgoValue, InputBox, OutBox, OutBoxBuilder}
import special.collection.Coll

import scala.jdk.CollectionConverters._


case class GameLPBoxBuilder(
                           gameLPBoxValue: Long,
                           gameLPBoxContract: ErgoContract,
                           gameLPSingletonToken: Eip4Token,
                           gameToken: Eip4Token,
                           devAddress: ErgoValue[Coll[java.lang.Byte]],
                           devFee: ErgoValue[(java.lang.Long, java.lang.Long)],
                           txOperatorFee: ErgoValue[(java.lang.Long, java.lang.Long)],
                           emissionInterval: ErgoValue[java.lang.Long],
                           emissionReductionFactorMultiplier: ErgoValue[java.lang.Long],
                           emissionReductionFactor: ErgoValue[java.lang.Long],
                           cardTokenBurnCount: ErgoValue[java.lang.Long],
                           cardTokenBurnTotal: ErgoValue[java.lang.Long]
                           ) extends TradeInBoxBuilder {
  override val value: Long = gameLPBoxValue
  override val contract: ErgoContract = gameLPBoxContract
  private var cardValueMappingTokens: Array[Eip4Token] = Array()

  override def toOutBox(implicit outBoxBuilder: OutBoxBuilder): OutBox = {

    val validTokens: Array[Eip4Token] = {
      if (cardValueMappingTokens.length > 0) {
        Array(gameLPSingletonToken, gameToken) ++ cardValueMappingTokens
      } else {
        Array(gameLPSingletonToken, gameToken)
      }
    }

    outBoxBuilder
      .value(gameLPBoxValue)
      .contract(gameLPBoxContract)
      .tokens(
        validTokens:_*
      )
      .registers(
        ErgoValue.pairOf(devAddress, ErgoValue.of(gameToken.getId.getBytes)),
        emissionInterval,
        emissionReductionFactorMultiplier,
        emissionReductionFactor,
        cardTokenBurnCount,
        cardTokenBurnTotal
      )
      .build()

  }

  def setCardValueMappingTokens(cardValueMappingInputBox: InputBox, newCardValueMappingToken: Eip4Token): Unit = {

    val currentTokens: List[String] = cardValueMappingInputBox.getTokens.asScala.toList.map((t: ErgoToken) => t.getId.toString)

    if (currentTokens.length > 2) {

     var prevCardValueMappingTokens: Array[Eip4Token] = Array()
      currentTokens.foreach( (id: String) =>
        prevCardValueMappingTokens = prevCardValueMappingTokens ++ Array(Eip4TokenBuilder.buildFromErgoBox(id, cardValueMappingInputBox))
      )

      this.cardValueMappingTokens = prevCardValueMappingTokens ++ Array(newCardValueMappingToken)

    } else {

      this.cardValueMappingTokens = Array(newCardValueMappingToken)

    }

  }

}

object GameLPBoxBuilder {

  def apply(input: InputBox)(implicit ctx: BlockchainContext): Option[GameLPBoxBuilder] = {

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
