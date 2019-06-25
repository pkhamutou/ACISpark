package alchemist.net

import cats.effect.{Concurrent, ContextShift, Resource}

import cats.Monad
import scodec.Encoder

import alchemist.net.message.{BackendMessage, Header}

trait MessageSocket[F[_]] {
  def receive: F[(Header, BackendMessage)]

  def send[A: Encoder](a: A): F[Unit]
}

object MessageSocket {

  def apply[F[_]: Concurrent: ContextShift](host: String, port: Int): Resource[F, MessageSocket[F]] =
    for {
      bvs <- BitVectorSocket[F](host, port)
    } yield new MessageSocketInterpreter[F](bvs)

  final class MessageSocketInterpreter[F[_]: Monad](bvs: BitVectorSocket[F]) extends MessageSocket[F] {
    import cats.syntax.flatMap._
    import cats.syntax.functor._

    override def receive: F[(Header, BackendMessage)] =
      bvs.read(10).flatMap { bits =>
        val header  = Header.codec.decodeValue(bits).require
        val decoder = BackendMessage.decode(header.command)

        bvs.read(header.size).map(bits => (header, decoder.decodeValue(bits).require))
      }

    override def send[A: Encoder](a: A): F[Unit] =
      bvs.write(Encoder[A].encode(a).require)
  }
}

