package alchemist.net

import scala.collection.immutable.NumericRange

import cats.effect.{Concurrent, ContextShift, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._

import cats.FlatMap
import org.apache.spark.mllib.linalg.distributed.IndexedRow

import alchemist.data.{Matrix, MatrixBlock, RowInfo, Worker}
import alchemist.net.message._
import alchemist.net.message.backend.RequestMatrixBlock


final class WorkerProtocol[F[_]: FlatMap](
  clientId: ClientId,
  sessionId: SessionId,
  worker: Worker,
  ms: MessageSocket[F]
) extends Serializable {

  def send(matrix: Matrix, indexedRows: Array[IndexedRow]): F[Header] = {

    println(s"send worker[${worker.id} ${this.hashCode()}, $clientId, $sessionId]")
    val blocks = sendIndexRows(worker.id.value, matrix, indexedRows)
    val header = Header.request(clientId, sessionId, Command.SendMatrixBlocks)

    val msg = SendMatrix(matrix.id, blocks.toVector)

    implicit val encoder: FrontendMessage[SendMatrix] = FrontendMessage.prefixed[SendMatrix](header)
    ms.send(msg).flatMap(_ => ms.receive).map {
      case (header, bm) => /*println(bm);*/ header
      case _ => throw new Exception("wtf")
    }
  }

  def get(mh: Matrix, rowIndices: Array[Long]) = {
    println(s"get worker[${worker.id} ${this.hashCode()}, $clientId, $sessionId]")
    val header = Header.request(clientId, sessionId, Command.RequestMatrixBlocks)

    val blocks = getIndexRows(mh, rowIndices)

    val msg = SendMatrix(mh.id, blocks.toVector)

    implicit val encoder: FrontendMessage[SendMatrix] = FrontendMessage.prefixed[SendMatrix](header)

    ms.send(msg).flatMap(_ => ms.receive).map {
      case (header, block: alchemist.net.message.backend.RequestMatrixBlock) => block
      case _ => throw new Exception("wtf")
    }
  }

  private def sendIndexRows(id: Short, mh: Matrix, indexedRows: Array[IndexedRow]): Array[MatrixBlock] = {
    val rowInfo = mh.getRowAssignments(id)
    val colInfo = mh.getColumnAssignments(id)

    val workerRows = rowInfo.start to rowInfo.end by rowInfo.step
    val workerCols = colInfo.start to colInfo.end by colInfo.step

    indexedRows
      .filter(row => workerRows.contains(row.index))
      .map(row => MatrixBlock(RowInfo.single(row.index), colInfo, row.vector.toArray.toVector))
  }

  def getIndexRows(mh: Matrix, rowIndices: Array[Long]) = {
    val rowInfo = mh.getRowAssignments(worker.id.value)
    val colInfo = mh.getColumnAssignments(worker.id.value)

    val workerRows = rowInfo.start to rowInfo.end by rowInfo.step
    val workerCols = colInfo.start to colInfo.end by colInfo.step

    rowIndices
      .filter(workerRows.contains)
      .map(row => MatrixBlock(RowInfo.single(row), colInfo, Vector.empty[Double]))
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

