package builders.transaction_builders

import org.ergoplatform.appkit.{Address, ErgoContract, OutBox, UnsignedTransaction, UnsignedTransactionBuilder}

trait TradeInTxBuilder {

  val txFee: Long
  val changeAddress: Address

  def build(implicit txBuilder: UnsignedTransactionBuilder): UnsignedTransaction

}
