package alchemist.net

import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.Executors

import scala.concurrent.duration.{DurationInt, FiniteDuration}

import cats.effect.{Concurrent, ContextShift, Resource}

import cats.MonadError
import fs2.Chunk
import fs2.io.tcp.Socket
import scodec.bits.BitVector

trait BitVectorSocket[F[_]] {
  def read(numBytes: Int): F[BitVector]

  def write(bits: BitVector): F[Unit]
}

object BitVectorSocket {

  final val DefaultAcg = AsynchronousChannelGroup.withThreadPool(Executors.newFixedThreadPool(2))

  def apply[F[_]: Concurrent: ContextShift](
    host: String,
    port: Int,
    readTimeout: FiniteDuration = 1.hour,
    writeTimeout: FiniteDuration = 1.hour,
    acg: AsynchronousChannelGroup = DefaultAcg
  ): Resource[F, BitVectorSocket[F]] = {

    implicit val acg0 = acg

    Socket
      .client[F](new InetSocketAddress(host, port))
      .map(socket => new BitVectorSocketInterpreter[F](socket, readTimeout, writeTimeout))
  }


  final class BitVectorSocketInterpreter[F[_]](
    socket: Socket[F],
    readTimeout: FiniteDuration,
    writeTimeout: FiniteDuration
  )(implicit F: MonadError[F, Throwable])
    extends BitVectorSocket[F] {

    override def read(numBytes: Int): F[BitVector] =
      F.flatMap(socket.readN(numBytes, Some(readTimeout))) {
        case None => F.raiseError(new Exception("Fatal: EOF"))
        case Some(chunk) =>
          if (chunk.size == numBytes) F.pure(BitVector(chunk.toArray))
          else F.raiseError(new Exception(s"Fatal: EOF before $numBytes bytes could be read.Bytes"))
      }

    override def write(bits: BitVector): F[Unit] =
      socket.write(Chunk.array(bits.toByteArray), Some(writeTimeout))
  }

}

