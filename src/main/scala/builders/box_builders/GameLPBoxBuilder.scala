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
                           tradeInFee: ErgoValue[(java.lang.Long, java.lang.Long)],
                           devFee: ErgoValue[(java.lang.Long, java.lang.Long)],
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
        ErgoValue.pairOf(tradeInFee, devFee),
        emissionInterval,
        emissionReductionFactorMultiplier,
        emissionReductionFactor,
        cardTokenBurnCount,
        cardTokenBurnTotal
      )
      .build()

  }

  def setCardValueMappingTokens(cardValueMappingInputBoxes: Array[InputBox], newCardValueMappingToken: Eip4Token): Unit = {

    val currentTokens: Array[String] = cardValueMappingInputBoxes.map((input: InputBox) => input.getTokens.asScala.toList(0).getId.toString)

    if (currentTokens.length > 1) {

      var prevCardValueMappingTokens: Array[Eip4Token] = Array()

      cardValueMappingInputBoxes.zipWithIndex.foreach{ case (input: InputBox, index: Int) =>
        prevCardValueMappingTokens = prevCardValueMappingTokens ++ Array(Eip4TokenBuilder.buildFromErgoBox(currentTokens(index), input))
      }

      this.cardValueMappingTokens = prevCardValueMappingTokens ++ Array(newCardValueMappingToken)

    } else {

      this.cardValueMappingTokens = Array(newCardValueMappingToken)

    }

  }

}

object GameLPBoxBuilder {

  def apply(input: InputBox)(implicit ctx: BlockchainContext): Option[GameLPBoxBuilder] = {

    if ((input.getTokens.size >= 2) & (input.getRegisters.size() == 6)) {

      val tradeInFee: (Long, Long) = input.getRegisters.get(0).asInstanceOf[ErgoValue[Coll[(Long, Long)]]].getValue()(0)
      val tradeInFeeErgoValue = ErgoValue.pairOf(ErgoValue.of(tradeInFee._1), ErgoValue.of(tradeInFee._2))
      
      val devFee: (Long, Long) = input.getRegisters.get(0).asInstanceOf[ErgoValue[Coll[(Long, Long)]]].getValue()(1)
      val devFeeErgoValue = ErgoValue.pairOf(ErgoValue.of(devFee._1), ErgoValue.of(devFee._2))

      Some(
        GameLPBoxBuilder(
          input.getValue,
          Address.fromErgoTree(input.getErgoTree, ctx.getNetworkType).toErgoContract,
          input.getTokens.get(0).asInstanceOf[Eip4Token],
          input.getTokens.get(1).asInstanceOf[Eip4Token],
          tradeInFeeErgoValue,
          devFeeErgoValue,
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
