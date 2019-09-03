package alchemist.net.message.backend

import scodec.Decoder

import alchemist.net.codecs.alchemistStringCodec
import alchemist.net.message.BackendMessage

final case class SingleString(value: String) extends BackendMessage

object SingleString {
  val decoder: Decoder[SingleString] = alchemistStringCodec.as[SingleString].decodeOnly
}
