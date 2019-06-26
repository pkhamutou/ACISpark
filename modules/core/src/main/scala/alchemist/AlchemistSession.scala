package alchemist

import cats.effect.{ Concurrent, ContextShift, Resource }
import cats.effect.concurrent.Ref

import cats.FlatMap
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.apply._
import com.typesafe.scalalogging.LazyLogging

import alchemist.data.Worker
import alchemist.net.Protocol
import alchemist.net.message.{ ClientId, SessionId }

class AlchemistSession[F[_]: FlatMap](
  clientId: ClientId,
  sessionId: SessionId,
  workersRef: Ref[F, List[Worker]],
  protocol: Protocol[F]
) extends LazyLogging {

  logger.info(s"My data is $clientId and $sessionId")

  def listAllWorkers(): F[List[Worker]] = protocol.listAllWorkers(clientId, sessionId)

  def listInactiveWorkers(): F[List[Worker]] = protocol.listInactiveWorkers(clientId, sessionId)

  def listActiveWorkers(): F[List[Worker]] = protocol.listActiveWorkers(clientId, sessionId)

  def listAssignedWorkers(): F[List[Worker]] = protocol.listAssignedWorkers(clientId, sessionId)

  def requestWorkers(numWorkers: Short): F[List[Worker]] =
    protocol.requestWorkers(clientId, sessionId, numWorkers).flatMap { workers =>
      workersRef.update(_ => workers).map(_ => workers)
    }

  def listRequestedWorkers(): F[List[Worker]] = workersRef.get

  def sendTestString(str: String): F[String] = protocol.sendTestString(clientId, sessionId, str)

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
      workers  <- Resource.liftF(Ref.of[F, List[Worker]](Nil))
      info     <- Resource.liftF(protocol.handshake())
    } yield new AlchemistSession[F](info.clientId, info.sessionId, workers, protocol)
}
