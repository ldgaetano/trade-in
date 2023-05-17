package builders.contract_builders

import org.ergoplatform.appkit.{BlockchainContext, ErgoContract}

trait TradeInContractBuilder {

  val script: String

  def toErgoContract(implicit ctx: BlockchainContext): ErgoContract

}
