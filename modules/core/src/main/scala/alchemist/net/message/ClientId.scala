package alchemist.net.message

import scodec.Codec
import scodec.codecs.{ StringEnrichedWithCodecContextSupport, short16 }

final case class ClientId(value: Short) extends AnyVal

object ClientId {
  implicit val codec: Codec[ClientId] = ("client_id" | short16).as[ClientId]
}
