package alchemist

import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._

import cats.Monad
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.mllib.linalg.distributed.{DistributedMatrix, IndexedRowMatrix}

import alchemist.data.{Library, Matrix, Worker}
import alchemist.library.Param
import alchemist.net.{Protocol, WorkerProtocol}
import alchemist.net.message.{ClientId, Header, Layout, SessionId}

final class AlchemistSession[F[_]: Sync: LiftIO](
  clientId: ClientId,
  sessionId: SessionId,
  workersRef: Ref[F, List[Worker]],
  librariesRef: Ref[F, List[Library]],
  protocol: Protocol[F]
) extends LazyLogging {

  logger.info(s"My data is $clientId and $sessionId")

  def listAllWorkers(): F[List[Worker]] = protocol.listAllWorkers(clientId, sessionId)

  def listInactiveWorkers(): F[List[Worker]] = protocol.listInactiveWorkers(clientId, sessionId)

  def listActiveWorkers(): F[List[Worker]] = protocol.listActiveWorkers(clientId, sessionId)

  def listAssignedWorkers(): F[List[Worker]] = protocol.listAssignedWorkers(clientId, sessionId)

  def requestWorkers(numWorkers: Short): F[List[Worker]] =
    protocol.requestWorkers(clientId, sessionId, numWorkers).flatMap { workers =>
      Monad[F].map(workersRef.update(_ => workers))(_ => workers)
    }

  def listRequestedWorkers(): F[List[Worker]] = workersRef.get

  def sendTestString(str: String): F[String] = protocol.sendTestString(clientId, sessionId, str)

  def loadLibrary(name: String, path: String): F[Library] =
    protocol.loadLibrary(clientId, sessionId, name, path).flatMap { library =>
      librariesRef.update(library :: _).flatMap(_ => Monad[F].pure(library))
    }

  def runTask(library: Library, methodName: String, args: List[Param]): F[List[Param]] =
    protocol.runTask(clientId, sessionId, library.id, methodName, args)

  def getMatrixHandle(matrix: DistributedMatrix): F[Matrix] =
    protocol.getMatrixHandle(clientId, sessionId, "Neo", matrix.numRows(), matrix.numCols(), 0, Layout.MC_MR)

  def sendIndexedRowMatrix(matrix: Matrix, indexedRowMatrix: IndexedRowMatrix): F[List[Header]] =
    workersRef.get.flatMap { workers =>
      indexedRowMatrix.rows.mapPartitions { rows =>
        val indexedRows = rows.toArray

        implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)

        val headers: IO[List[Header]] = workers
          .traverse(WorkerProtocol.make[IO])
          .use { wps: List[WorkerProtocol[IO]] =>
            wps.traverse((wp: WorkerProtocol[IO]) => wp.send(matrix, indexedRows))
          }

        headers.unsafeRunSync().toIterator
      }
      .collect()
      .toList
      .pure[F]
    }


  def close(): Unit =
    logger.info("closing alchemist session!")
}

object AlchemistSession extends LazyLogging {

  /**
    * Closes resources in the opposite direction.
    */
  def make[F[_]](
    host: String,
    port: Int
  )(implicit F: Concurrent[F], CS: ContextShift[F]): Resource[F, AlchemistSession[F]] =
    for {
      protocol  <- Protocol[F](host, port)
      workers   <- Resource.liftF(Ref.of[F, List[Worker]](Nil))
      libraries <- Resource.liftF(Ref.of[F, List[Library]](Nil))
      info      <- Resource.liftF(protocol.handshake())
    } yield new AlchemistSession[F](info.clientId, info.sessionId, workers, libraries, protocol)
}
