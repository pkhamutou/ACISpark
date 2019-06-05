package alchemist

import cats.effect._
import com.typesafe.scalalogging.LazyLogging
import java.net.Socket

class AlchemistSession(socket: Socket) extends LazyLogging {

  def close(): Unit = logger.info("closing alchemist session!")
}

object AlchemistSession extends LazyLogging {

  private def acquire[F[_]: Sync](socket: Socket): F[AlchemistSession] =
    Sync[F].delay(new AlchemistSession(socket))

  private def release[F[_]: Sync](as: AlchemistSession): F[Unit] =
    Sync[F].delay(as.close())

  /**
    * Closes resources in the opposite direction.
    */
  def make[F[_]](host: String, port: Int)(implicit F: Sync[F]): Resource[F, AlchemistSession] =
    for {
      socket  <- Resource.fromAutoCloseable(F.delay(new Socket(host, port)))
      session <- Resource.make(acquire[F](socket))(release[F])
    } yield session
}
