package builders.box_builders
import configs.report_config.TradeInReportConfig
import configs.setup_config.TradeInSetupConfig
import org.ergoplatform.appkit.{Address, BlockchainContext, Eip4Token, ErgoContract, ErgoValue, InputBox, NetworkType, OutBox, OutBoxBuilder}
import special.collection.Coll
import utils.TradeInUtils

case class GameLPBoxBuilder(
                           gameLPBoxValue: Long,
                           gameLPBoxContract: ErgoContract,
                           gameLPSingletonToken: Eip4Token,
                           gameToken: Eip4Token,
                           devAddress: ErgoValue[Coll[java.lang.Byte]],
                           emissionInterval: ErgoValue[java.lang.Long],
                           emissionReductionFactorMultiplier: ErgoValue[java.lang.Long],
                           emissionReductionFactor: ErgoValue[java.lang.Long],
                           cardTokenBurnCount: ErgoValue[java.lang.Long],
                           cardTokenBurnTotal: ErgoValue[java.lang.Long]
                           ) extends TradeInBoxBuilder {
  override val value: Long = gameLPBoxValue
  override val contract: ErgoContract = gameLPBoxContract

  override def toOutBox(implicit outBoxBuilder: OutBoxBuilder): OutBox = {

    outBoxBuilder
      .value(gameLPBoxValue)
      .contract(gameLPBoxContract)
      .tokens(
        gameLPSingletonToken,
        gameToken
      )
      .registers(
        devAddress,
        emissionInterval,
        emissionReductionFactorMultiplier,
        emissionReductionFactor,
        cardTokenBurnCount,
        cardTokenBurnTotal
      )
      .build()

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
