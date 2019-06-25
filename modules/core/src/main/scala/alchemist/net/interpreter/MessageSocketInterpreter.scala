package alchemist.net.interpreter

import cats.Monad
import cats.syntax.flatMap._
import cats.syntax.functor._

import alchemist.net.{ BitVectorSocket, MessageSocket }
import alchemist.net.message.{ BackendMessage, FrontendMessage, Header }

private[net] class MessageSocketInterpreter[F[_]: Monad](bvs: BitVectorSocket[F]) extends MessageSocket[F] {

  override def receive: F[(Header, BackendMessage)] =
    bvs.read(Header.HeaderLength).flatMap { bits =>
      val header  = Header.codec.decodeValue(bits).require
      val decoder = BackendMessage.decode(header.command)

      bvs.read(header.size).map(bits => (header, decoder.decodeValue(bits).require))
    }

  override def send[A](a: A)(implicit fm: FrontendMessage[A]): F[Unit] =
    bvs.write(fm.encoder.encode(a).require)
}
