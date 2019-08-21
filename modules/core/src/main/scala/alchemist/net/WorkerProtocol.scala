package alchemist.net

import scala.collection.immutable.NumericRange

import cats.effect.{Concurrent, ContextShift, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._

import cats.FlatMap
import org.apache.spark.mllib.linalg.distributed.IndexedRow

import alchemist.data.{Matrix, MatrixBlock, Worker}
import alchemist.net.message._


object sender {

  def sendIndexRows(id: Short, mh: Matrix, indexedRows: Array[IndexedRow]): (MatrixBlock, Array[Double]) = {
    val rows = mh.getRowAssignments(id)
    val cols = mh.getColumnAssignments(id)

    val numRows: Long = math.ceil((rows(1) - rows(0) + 1) / rows(2)).toLong
    val numCols: Long = math.ceil((cols(1) - cols(0) + 1) / cols(2)).toLong

    val numElements: Long = numRows * numCols

    val index = indexedRows.head.index
    val start = index + (rows(0) + (index % rows(2))) % rows(2)
    val end = indexedRows.last.index

    val messageRows: Array[Long] = Array(start, end, rows(2))
    val block: MatrixBlock = MatrixBlock(Array.empty[Double], messageRows, cols)

    val rowIndices = NumericRange[Long](start, end, 2).toSet[Long]
    val colIndices = NumericRange[Long](cols(0), cols(1), cols(2)).map(_.toInt)

    val r = indexedRows
      .filter(row => rowIndices.contains(row.index))
      .map(_.vector.toArray)
      .flatMap { xs =>
        colIndices.foldLeft(Array.emptyDoubleArray) {
          case (z, i) => z :+ xs(i)
        }
      }

    (block, r)
  }
}

final class WorkerProtocol[F[_]: FlatMap](
  clientId: ClientId,
  sessionId: SessionId,
  worker: Worker,
  ms: MessageSocket[F]
) extends Serializable {

  def send(matrix: Matrix, indexedRows: Array[IndexedRow]): F[Header] = {

    println(s"worker[${worker.id} ${this.hashCode()}, $clientId, $sessionId]")
    val (mb, data) = sendIndexRows(worker.id.value, matrix, indexedRows)
    val header = Header.request(clientId, sessionId, Command.SendMatrixBlocks)

    val msg = SendMatrix(matrix.id, mb, data.toVector)

    implicit val encoder: FrontendMessage[SendMatrix] = FrontendMessage.prefixed[SendMatrix](header)
    ms.send(msg).flatMap(_ => ms.receive).map {
      case (header, bm) => println(bm); header
      case _ => throw new Exception("wtf")
    }
  }

  private def sendIndexRows(id: Short, mh: Matrix, indexedRows: Array[IndexedRow]): (MatrixBlock, Array[Double]) = {
    val rows = mh.getRowAssignments(id)
    val cols = mh.getColumnAssignments(id)

    val numRows: Long = math.ceil((rows(1) - rows(0) + 1) / rows(2)).toLong
    val numCols: Long = math.ceil((cols(1) - cols(0) + 1) / cols(2)).toLong

    val numElements: Long = numRows * numCols

    val index = indexedRows.head.index
    val start = index + (rows(0) + (index % rows(2))) % rows(2)
    val end = indexedRows.last.index

    val messageRows: Array[Long] = Array(start, end, rows(2))
    val block: MatrixBlock = MatrixBlock(Array.empty[Double], messageRows, cols)

    val rowIndices = NumericRange[Long](start, end, 2).toSet[Long]
    val colIndices = NumericRange[Long](cols(0), cols(1), cols(2)).map(_.toInt)

    val r = indexedRows
      .filter(row => rowIndices.contains(row.index))
      .map(_.vector.toArray)
      .flatMap { xs =>
        colIndices.foldLeft(Array.emptyDoubleArray) {
          case (z, i) => z :+ xs(i)
        }
      }

    (block, r)
  }
}

object WorkerProtocol {

  def make[F[_]: Concurrent: ContextShift](worker: Worker): Resource[F, WorkerProtocol[F]] =
    for {
      ms       <- MessageSocket[F](worker.hostname, worker.port)
      protocol <- Protocol[F](worker.hostname, worker.port)
      info     <- Resource.liftF(protocol.handshake())
    } yield new WorkerProtocol[F](info.clientId, info.sessionId, worker, ms)
}

