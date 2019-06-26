package alchemist.net

import cats.effect.{Concurrent, ContextShift, Resource}

import alchemist.data.Worker
import alchemist.net.interpreter.ProtocolInterpreter
import alchemist.net.message.{ClientId, ConnectionInfo, SessionId}

trait Protocol[F[_]] {

  def handshake(): F[ConnectionInfo]

  def listAllWorkers(clientId: ClientId, sessionId: SessionId): F[List[Worker]]

  def listInactiveWorkers(clientId: ClientId, sessionId: SessionId): F[List[Worker]]

  def listActiveWorkers(clientId: ClientId, sessionId: SessionId): F[List[Worker]]

  def listAssignedWorkers(clientId: ClientId, sessionId: SessionId): F[List[Worker]]

  def requestWorkers(clientId: ClientId, sessionId: SessionId, numWorkers: Short): F[List[Worker]]

  def sendTestString(clientId: ClientId, sessionId: SessionId, str: String): F[String]
}

object Protocol {

  def apply[F[_]: Concurrent: ContextShift](host: String, port: Int): Resource[F, Protocol[F]] =
    for {
      ms <- MessageSocket[F](host, port)
    } yield new ProtocolInterpreter[F](ms)
}
