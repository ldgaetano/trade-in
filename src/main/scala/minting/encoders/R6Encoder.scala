package minting.encoders

import org.ergoplatform.appkit.ErgoValue

trait R6Encoder[T, U] {

  def encodeR6(data: T): ErgoValue[U]

}
