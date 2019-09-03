package alchemist.net.interpreter

import scala.concurrent.duration.FiniteDuration

import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._

import cats.MonadError
import fs2.io.tcp.Socket
import fs2.Chunk
import scodec.bits.BitVector

import alchemist.net.BitVectorSocket

private[net] class BitVectorSocketInterpreter[F[_]: MonadError[?[_], Throwable]](
  socket: Socket[F],
  readTimeout: FiniteDuration,
  writeTimeout: FiniteDuration
) extends BitVectorSocket[F] {

  override def read(numBytes: Int): F[BitVector] =
    socket.readN(numBytes, Some(readTimeout)).flatMap {
      case None => new Exception("Fatal: EOF").raiseError[F, BitVector]
      case Some(chunk) =>
        if (chunk.size == numBytes) BitVector(chunk.toArray).pure[F]
        else new Exception(s"Fatal: EOF before $numBytes bytes could be read.Bytes").raiseError[F, BitVector]
    }

  override def write(bits: BitVector): F[Unit] =
    socket.write(Chunk.array(bits.toByteArray), Some(writeTimeout))
}
