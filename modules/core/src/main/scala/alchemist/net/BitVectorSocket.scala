package alchemist.net

import java.net.InetSocketAddress
import java.nio.channels.AsynchronousChannelGroup
import java.util.concurrent.Executors

import scala.concurrent.duration.{ DurationInt, FiniteDuration }

import cats.effect.{ Concurrent, ContextShift, Resource }

import fs2.io.tcp.Socket
import scodec.bits.BitVector

import alchemist.net.interpreter.BitVectorSocketInterpreter

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

    implicit val acg0: AsynchronousChannelGroup = acg

    Socket
      .client[F](new InetSocketAddress(host, port))
      .map(socket => new BitVectorSocketInterpreter[F](socket, readTimeout, writeTimeout))
  }

}
