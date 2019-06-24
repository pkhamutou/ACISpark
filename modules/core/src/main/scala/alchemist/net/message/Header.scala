package alchemist.net.message

import scodec.{ Codec, Encoder }
import scodec.codecs._

import alchemist.net.Command

final case class ClientId(value: Short) extends AnyVal

object ClientId {
  implicit val codec: Codec[ClientId] = ("client_id" | short16).as[ClientId]
}

final case class SessionId(value: Short) extends AnyVal

object SessionId {
  implicit val codec: Codec[SessionId] = ("session_id" | short16).as[SessionId]
}

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
