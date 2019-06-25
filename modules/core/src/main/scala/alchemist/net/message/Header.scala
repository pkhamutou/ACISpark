package alchemist.net.message

import scodec.{ Codec, Decoder, Encoder }
import scodec.codecs._

final case class Header(clientId: ClientId, sessionId: SessionId, command: Command, error: Byte, size: Int)

object Header {

  final val HeaderLength: Int = 10

  def request(clientId: ClientId, sessionId: SessionId, command: Command): Header =
    new Header(clientId, sessionId, command, 0, 0)

  // format: off
  private val encoder: Encoder[Header] = 
    "header" | fixedSizeBytes(HeaderLength - 4,
      ("client_id"  | Codec[ClientId]) ~
      ("session_id" | Codec[SessionId]) ~
      ("command"    | Codec[Command]) ~
      ("error"      | byte)
    ).contramap[Header](h => h.clientId ~ h.sessionId ~ h.command ~ h.error).encodeOnly

  private val decoder: Decoder[Header] =
    "header" | fixedSizeBytes(HeaderLength,
      ("client_id"  | Codec[ClientId])  ::
      ("session_id" | Codec[SessionId]) ::
      ("command"    | Codec[Command])   ::
      ("error"      | byte)             ::
      ("size"       | int32)
    ).as[Header].decodeOnly
  
  implicit val codec: Codec[Header] = Codec[Header](encoder, decoder)
  // format: on

  implicit val HeaderFrontendMessage: FrontendMessage[Header] = new FrontendMessage[Header] {
    override def encoder: Encoder[Header] = (Header.codec <~ int32.unit(0)).encodeOnly
  }
}
