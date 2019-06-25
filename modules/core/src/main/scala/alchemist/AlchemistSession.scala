package alchemist

import cats.effect.{Concurrent, ContextShift, Resource}

import com.typesafe.scalalogging.LazyLogging

import alchemist.data.Worker
import alchemist.net.Protocol
import alchemist.net.message.{ClientId, SessionId}

class AlchemistSession[F[_]](clientId: ClientId, sessionId: SessionId, protocol: Protocol[F]) extends LazyLogging {

  logger.info(s"My data is $clientId and $sessionId")

  def listAllWorkers(): F[List[Worker]] = protocol.listAllWorkers(clientId, sessionId)

  def listInactiveWorkers(): F[List[Worker]] = protocol.listInactiveWorkers(clientId, sessionId)

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
      protocol <- Protocol[F](host, port)
      info     <- Resource.liftF(protocol.handshake())
    } yield new AlchemistSession[F](info.clientId, info.sessionId, protocol)
}
