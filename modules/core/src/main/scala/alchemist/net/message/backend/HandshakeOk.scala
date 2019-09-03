package alchemist.net.message.backend

import scodec.Decoder
import scodec.codecs.StringEnrichedWithCodecContextSupport

import alchemist.net.codecs.{alchemistDoubleCodec, alchemistShortCodec, alchemistStringCodec}
import alchemist.net.message.BackendMessage

case class HandshakeOk(short: Short, string: String, double: Double) extends BackendMessage

object HandshakeOk {

  // format: off
  val decoder: Decoder[HandshakeOk] =  {
    "handshake_ok" |
      ("short"  | alchemistShortCodec)  ::
      ("string" | alchemistStringCodec) ::
      ("double" | alchemistDoubleCodec)
  }.as[HandshakeOk].decodeOnly
  // format: on

}
