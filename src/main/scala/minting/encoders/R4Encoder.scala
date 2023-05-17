package minting.encoders

import org.ergoplatform.appkit.ErgoValue

trait R4Encoder[T, U] {

  def encodeR4(data: T): ErgoValue[U]

}
