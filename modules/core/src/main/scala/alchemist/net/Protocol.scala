package alchemist.net

import cats.effect.{Concurrent, ContextShift, Resource}

import alchemist.data.{Library, Matrix, Worker}
import alchemist.library.Param
import alchemist.net.interpreter.ProtocolInterpreter
import alchemist.net.message.{ClientId, ConnectionInfo, Layout, SessionId}

trait Protocol[F[_]] {

  def handshake(): F[ConnectionInfo]

  def listAllWorkers(clientId: ClientId, sessionId: SessionId): F[List[Worker]]

  def listInactiveWorkers(clientId: ClientId, sessionId: SessionId): F[List[Worker]]

  def listActiveWorkers(clientId: ClientId, sessionId: SessionId): F[List[Worker]]

  def listAssignedWorkers(clientId: ClientId, sessionId: SessionId): F[List[Worker]]

  def requestWorkers(clientId: ClientId, sessionId: SessionId, numWorkers: Short): F[List[Worker]]

  def sendTestString(clientId: ClientId, sessionId: SessionId, str: String): F[String]

  def loadLibrary(clientId: ClientId, sessionId: SessionId, name: String, path: String): F[Library]

  def runTask(
    clientId: ClientId,
    sessionId: SessionId,
    libraryId: Library.LibraryId,
    methodName: String,
    args: List[Param]
  ): F[List[Param]]

  def getMatrixHandle(
    clientId: ClientId,
    sessionId: SessionId,
    matrixName: String,
    numOfRows: Long,
    numOfColumns: Long,
    sparse: Byte,
    layout: Layout
  ): F[Matrix]
}

object Protocol {

  def apply[F[_]: Concurrent: ContextShift](host: String, port: Int): Resource[F, Protocol[F]] =
    for {
      ms <- MessageSocket[F](host, port)
    } yield new ProtocolInterpreter[F](ms)
}
