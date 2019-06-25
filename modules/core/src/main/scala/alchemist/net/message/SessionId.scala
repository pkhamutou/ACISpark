package alchemist.net.message

import scodec.Codec
import scodec.codecs.{ StringEnrichedWithCodecContextSupport, short16 }

final case class SessionId(value: Short) extends AnyVal

object SessionId {
  implicit val codec: Codec[SessionId] = ("session_id" | short16).as[SessionId]
}
