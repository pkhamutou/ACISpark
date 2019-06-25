package alchemist

import org.scalatest.WordSpec
import scodec._
import scodec.bits.BitVector
import scodec.codecs._

import alchemist.net.message._

class ScodecTest extends WordSpec {

  case class HeaderRequest(clientId: ClientId, sessionId: SessionId, command: Command)

  val s = (Codec[ClientId] ~ Codec[SessionId] ~ Codec[Command])

//  Header.codec.asEncoder.contramap()
//
//  val p = Encoder { header: Header =>
//    for {
//      c  <- Encoder[ClientId].encode(header.clientId)
//      s  <- Encoder[SessionId].encode(header.sessionId)
//      cd <- Encoder[Command].encode(header.command)
//      er <- byte.encode(0)
//    } yield c ++ s ++ cd ++ er
//  }

  val header = Header.request(ClientId(1), SessionId(3), Command.Handshake)

  println(Header.codec.encode(header).require.toByteArray.toList)
  println((Header.codec <~ int32.unit(5)).encode(header).require.toByteArray.toList)

}
