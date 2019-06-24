package alchemist.net.message

import scodec.Encoder
import scodec.codecs.StringEnrichedWithCodecContextSupport

import alchemist.data.MatrixBlock
import alchemist.net.codecs._

final case class Handshake(
  byte: Byte,
  short: Short,
  string: String,
  double1: Double,
  double2: Double,
  matrixBlock: MatrixBlock,
  size: Int
)

object Handshake {

  // format: off
  val encoder: Encoder[Handshake] = {
    "handshake" |
      ("byte"         | alchemistByteCodec)        ::
      ("short"        | alchemistShortCodec)       ::
      ("string"       | alchemistStringCodec)      ::
      ("double_1"     | alchemistDoubleCodec)      ::
      ("double_2"     | alchemistDoubleCodec)      ::
      ("matrix_block" | alchemistMatrixBlockCodec) ::
      ("size"         | alchemistIntCodec)
  }.as[Handshake].asEncoder
  // format: on

}
