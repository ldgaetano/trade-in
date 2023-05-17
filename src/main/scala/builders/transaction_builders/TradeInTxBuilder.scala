package builders.transaction_builders

import org.ergoplatform.appkit.{ErgoContract, UnsignedTransaction, UnsignedTransactionBuilder}

trait TradeInTxBuilder {

  val txFee: Long

  def build(implicit txBuilder: UnsignedTransactionBuilder): UnsignedTransaction

}
