package alchemist

import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._

import cats.Monad
import com.typesafe.scalalogging.LazyLogging
import org.apache.spark.mllib.linalg.distributed.{DistributedMatrix, IndexedRow, IndexedRowMatrix}
import org.apache.spark.mllib.linalg.DenseVector
import org.apache.spark.rdd.RDD

import alchemist.data.{Library, Matrix, MatrixBlock, Worker}
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
    protocol.getMatrixHandle(clientId, sessionId, "Neo", matrix.numRows(), matrix.numCols(), 0, Layout.VC_STAR)

  def sendIndexedRowMatrix(matrix: Matrix, indexedRowMatrix: IndexedRowMatrix): F[List[Header]] =
    workersRef.get.flatMap { workers =>
      println("PARTITIONS: " + indexedRowMatrix.rows.getNumPartitions)
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

  def getIndexRowMatrix(spark: org.apache.spark.sql.SparkSession, matrix: Matrix): F[RDD[IndexedRow]] = {
    val rows = spark.sparkContext.parallelize(0L until matrix.numOfRows).repartition(4)
    println("PARTITIONS: " + rows.getNumPartitions)

    val f: Vector[MatrixBlock] => Vector[IndexedRow] =
      _.map(mb => IndexedRow(mb.index, new DenseVector(mb.data.toArray)))

    workersRef.get.flatMap { workers =>
      rows
        .mapPartitionsWithIndex { (idx, d) =>
          val indices                       = d.toArray
          implicit val cs: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)

          val result = workers.traverse(WorkerProtocol.make[IO]).use { wps =>
            wps.traverse(wp => wp.get(matrix, indices))
          }

          result
            .map(_.map(b => f(b.blocks)))
            .unsafeRunSync()
            .flatten
            .toIterator
        }
        .pure[F]

    }
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
