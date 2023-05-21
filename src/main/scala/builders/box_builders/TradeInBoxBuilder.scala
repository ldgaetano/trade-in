package builders.box_builders

import org.ergoplatform.appkit.{BlockchainContext, ErgoContract, InputBox, NetworkType, OutBox, OutBoxBuilder, UnsignedTransactionBuilder}

trait TradeInBoxBuilder {

  val value: Long
  val contract: ErgoContract

  def toOutBox(implicit outBoxBuilder: OutBoxBuilder): OutBox

}
