package alchemist.net.interpreter

import cats.syntax.flatMap._
import cats.syntax.functor._

import cats.FlatMap

import alchemist.data._
import alchemist.library.Param
import alchemist.net.{ MessageSocket, Protocol }
import alchemist.net.message._
import alchemist.net.message.backend.{ GetLibraryId, HandshakeOk, ListWorkers, SingleString }

private[net] class ProtocolInterpreter[F[_]: FlatMap](ms: MessageSocket[F]) extends Protocol[F] {

  override def handshake(): F[ConnectionInfo] = {

    val matrix = MatrixBlock(
      row = RowInfo(0, 3, 1),
      column = ColumnInfo(0, 2, 1),
      data = (3 to 14).map(_ * 1.11d).toVector
    )

    val handshake = Handshake(2, 1234, "ABCD", 1.11d, 2.22d, matrix, 190)
    val header    = Header.request(ClientId(0), SessionId(0), Command.Handshake)

    implicit val encoder: FrontendMessage[Handshake] = FrontendMessage.prefixed[Handshake](header)

    ms.send(handshake).flatMap(_ => ms.receive).map {
      case (h: Header, _: HandshakeOk) => ConnectionInfo(h.clientId, h.sessionId)
      case _                           => throw new Exception("Boom!")
    }
  }

  private def listWorkers(clientId: ClientId, sessionId: SessionId, command: Command): F[List[Worker]] =
    ms.send(Header.request(clientId, sessionId, command)).flatMap(_ => ms.receive).map {
      case (_, wrs: ListWorkers) => wrs.workers
      case _                     => throw new Exception("Boom!")
    }

  override def listAllWorkers(clientId: ClientId, sessionId: SessionId): F[List[Worker]] =
    listWorkers(clientId, sessionId, Command.ListAllWorkers)

  override def listInactiveWorkers(clientId: ClientId, sessionId: SessionId): F[List[Worker]] =
    listWorkers(clientId, sessionId, Command.ListInactiveWorkers)

  override def listActiveWorkers(clientId: ClientId, sessionId: SessionId): F[List[Worker]] =
    listWorkers(clientId, sessionId, Command.ListActiveWorkers)

  override def listAssignedWorkers(clientId: ClientId, sessionId: SessionId): F[List[Worker]] =
    listWorkers(clientId, sessionId, Command.ListAssignedWorkers)

  override def requestWorkers(clientId: ClientId, sessionId: SessionId, numWorkers: Short): F[List[Worker]] = {
    val header = Header.request(clientId, sessionId, Command.RequestWorkers)

    implicit val encoder: FrontendMessage[RequestWorkers] = FrontendMessage.prefixed[RequestWorkers](header)

    ms.send(RequestWorkers(numWorkers)).flatMap(_ => ms.receive).map {
      case (_, wrs: ListWorkers) => wrs.workers
      case _                     => throw new Exception("Boom!")
    }
  }

  override def sendTestString(clientId: ClientId, sessionId: SessionId, str: String): F[String] = {
    val header = Header.request(clientId, sessionId, Command.SendTestString)

    implicit val encoder: FrontendMessage[String] =
      FrontendMessage.prefixed[String](header)(alchemist.net.codecs.alchemistStringCodec)

    ms.send(str).flatMap(_ => ms.receive).map {
      case (_, str: SingleString) => str.value
      case _                      => throw new Exception("Boom!")
    }
  }

  def loadLibrary(clientId: ClientId, sessionId: SessionId, name: String, path: String): F[Library] = {
    val header = Header.request(clientId, sessionId, Command.LoadLibrary)

    implicit val encoder: FrontendMessage[LoadLibrary] = FrontendMessage.prefixed[LoadLibrary](header)

    ms.send(LoadLibrary(name, path)).flatMap(_ => ms.receive).map {
      case (_, id: GetLibraryId) => Library(id.value, name)
      case _                     => throw new Exception("Boom!")
    }
  }

  def runTask(
    clientId: ClientId,
    sessionId: SessionId,
    libraryId: Library.LibraryId,
    methodName: String,
    args: List[Param]
  ): F[List[Param]] = {
    val header = Header.request(clientId, sessionId, Command.RunTask)

    implicit val encoder: FrontendMessage[RunTask] = FrontendMessage.prefixed[RunTask](header)

    ms.send(RunTask(libraryId, methodName, args)).flatMap(_ => ms.receive).map {
      case (_, alchemist.net.message.backend.RunTask(args0)) => args0
      case _                                                 => throw new Exception("Boom!")
    }

  }

  def getMatrixHandle(
    clientId: ClientId,
    sessionId: SessionId,
    matrixName: String,
    numOfRows: Long,
    numOfColumns: Long,
    sparse: Byte,
    layout: Layout
  ): F[Matrix] = {
    val header = Header.request(clientId, sessionId, Command.MatrixInfo)

    implicit val encoder: FrontendMessage[MatrixInfo] = FrontendMessage.prefixed[MatrixInfo](header)

    ms.send(MatrixInfo(matrixName, numOfRows, numOfColumns, sparse, layout)).flatMap(_ => ms.receive).map {
      case (_, alchemist.net.message.backend.GetMatrixHandle(matrix)) => matrix
      case _                                                          => throw new Exception("Boom!")
    }
  }
}
