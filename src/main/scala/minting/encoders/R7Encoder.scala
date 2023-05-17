package minting.encoders

import org.ergoplatform.appkit.ErgoValue

trait R7Encoder[T, U] {

  def encodeR7(data: T): ErgoValue[U]

}
