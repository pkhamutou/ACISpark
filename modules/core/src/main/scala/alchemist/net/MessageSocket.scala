package alchemist.net

import cats.effect.{Concurrent, ContextShift, Resource}

import scodec.Encoder

import alchemist.net.interpreter.MessageSocketInterpreter
import alchemist.net.message.{BackendMessage, FrontendMessage, Header}

trait MessageSocket[F[_]] extends Serializable {
  def receive: F[(Header, BackendMessage)]

  def send[A: FrontendMessage](a: A): F[Unit]
}

object MessageSocket {

  def apply[F[_]: Concurrent: ContextShift](host: String, port: Int): Resource[F, MessageSocket[F]] =
    for {
      bvs <- BitVectorSocket[F](host, port)
    } yield new MessageSocketInterpreter[F](bvs)

}

