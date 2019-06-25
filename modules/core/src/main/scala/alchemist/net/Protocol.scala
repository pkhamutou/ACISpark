package alchemist.net

import cats.effect.{Concurrent, ContextShift, Resource}

import alchemist.net.interpreter.ProtocolInterpreter
import alchemist.net.message.ConnectionInfo

trait Protocol[F[_]] {

  def handshake(): F[ConnectionInfo]

}

object Protocol {

  def apply[F[_]: Concurrent: ContextShift](host: String, port: Int): Resource[F, Protocol[F]] =
    for {
      ms <- MessageSocket[F](host, port)
    } yield new ProtocolInterpreter[F](ms)
}
