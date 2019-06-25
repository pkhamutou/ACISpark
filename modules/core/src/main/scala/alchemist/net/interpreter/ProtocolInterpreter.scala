package alchemist.net.interpreter

import cats.syntax.flatMap._
import cats.syntax.functor._

import cats.FlatMap

import alchemist.net.{ MessageSocket, Protocol }
import alchemist.net.message._

private[net] class ProtocolInterpreter[F[_]: FlatMap](ms: MessageSocket[F]) extends Protocol[F] {

  override def handshake(): F[ConnectionInfo] = {

    val matrix = alchemist.data.MatrixBlock(
      data = (3 to 14).map(_ * 1.11d).toArray,
      rows = Array(0, 3, 1),
      columns = Array(0, 2, 1)
    )

    val handshake = Handshake(2, 1234, "ABCD", 1.11d, 2.22d, matrix, 190)
    val header    = Header(ClientId(0), SessionId(0), Command.Handshake, 0, 0)
    import scodec.Encoder

    val encoder = Encoder { a: (Header, Handshake) =>
      val (h, hs) = a
      for {
        hsBits <- Handshake.encoder.encode(hs)
        size   <- scodec.codecs.int32.encode((hsBits.size / 8).toInt)
        hBits  <- Header.codec.encode(h)
      } yield hBits.dropRight(4 * 8) ++ size ++ hsBits
    }

    ms.send((header, handshake))(encoder).flatMap(_ => ms.receive).map {
      case (h: Header, hsOk: HandshakeOk) => println(hsOk); ConnectionInfo(h.clientId, h.sessionId)
      case _                              => throw new Exception("Boom!")
    }
  }
}
