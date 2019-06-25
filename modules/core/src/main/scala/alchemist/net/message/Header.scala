package alchemist.net.message

import scodec.Codec
import scodec.codecs._

final case class Header(clientId: ClientId, sessionId: SessionId, command: Command, error: Byte, size: Int)

object Header {

  final val HeaderLength: Int = 10

  // format: off
  implicit val codec: Codec[Header] =
    "header" | fixedSizeBytes(HeaderLength,
      ("client_id"  | Codec[ClientId])  ::
      ("session_id" | Codec[SessionId]) ::
      ("command"    | Codec[Command])   ::
      ("error"      | byte)             ::
      ("size"       | int32)
    ).as[Header]
  // format: on

}
