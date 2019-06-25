package alchemist.net.message

import scodec.Decoder
import scodec.codecs.StringEnrichedWithCodecContextSupport

import alchemist.net.codecs.{alchemistDoubleCodec, alchemistShortCodec, alchemistStringCodec}

trait BackendMessage

object BackendMessage {

  def decode(c: Command): Decoder[BackendMessage] = c match {
    case Command.Handshake => HandshakeOk.decoder

    case _ => ???
  }
}

case class HandshakeOk(short: Short, string: String, double: Double) extends BackendMessage

object HandshakeOk {

  // format: off
  val decoder: Decoder[HandshakeOk] =  {
    "handshake_ok" |
      ("short"  | alchemistShortCodec)  ::
      ("string" | alchemistStringCodec) ::
      ("double" | alchemistDoubleCodec)
  }.as[HandshakeOk].asDecoder
  // format: on

}
