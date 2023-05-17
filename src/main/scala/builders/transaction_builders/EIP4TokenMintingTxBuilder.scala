package builders.transaction_builders

import org.ergoplatform.appkit.{InputBox, OutBox}

abstract class EIP4TokenMintingTxBuilder extends TradeInTxBuilder {

  val eip4IssuanceBox: OutBox

}
