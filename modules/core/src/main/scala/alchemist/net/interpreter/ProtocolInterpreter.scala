package alchemist.net.interpreter

import cats.syntax.flatMap._
import cats.syntax.functor._

import cats.FlatMap

import alchemist.data.Worker
import alchemist.net.{ MessageSocket, Protocol }
import alchemist.net.message._
import alchemist.net.message.backend.{ HandshakeOk, ListAllWorkers }

private[net] class ProtocolInterpreter[F[_]: FlatMap](ms: MessageSocket[F]) extends Protocol[F] {

  override def handshake(): F[ConnectionInfo] = {

    val matrix = alchemist.data.MatrixBlock(
      data = (3 to 14).map(_ * 1.11d).toArray,
      rows = Array(0, 3, 1),
      columns = Array(0, 2, 1)
    )

    val handshake = Handshake(2, 1234, "ABCD", 1.11d, 2.22d, matrix, 190)
    val header    = Header.request(ClientId(0), SessionId(0), Command.Handshake)

    implicit val encoder: FrontendMessage[Handshake] = FrontendMessage.prefixed[Handshake](header)

    ms.send(handshake).flatMap(_ => ms.receive).map {
      case (h: Header, hsOk: HandshakeOk) => println(hsOk); ConnectionInfo(h.clientId, h.sessionId)
      case _                              => throw new Exception("Boom!")
    }
  }

  override def listAllWorkers(clientId: ClientId, sessionId: SessionId): F[List[Worker]] = {
    val header = Header.request(clientId, sessionId, Command.ListAllWorkers)

    ms.send(header).flatMap(_ => ms.receive).map {
      case (_, wrs: ListAllWorkers) => wrs.workers
      case _                        => throw new Exception("Boom!")
    }
  }
}
