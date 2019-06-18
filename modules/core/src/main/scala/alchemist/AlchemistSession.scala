package alchemist

import java.net.Socket

import cats.effect.{ Resource, Sync }

import com.typesafe.scalalogging.LazyLogging

class AlchemistSession(socket: Socket) extends LazyLogging {

  def handshake(): Unit = {
    val msg = alchemist.net.Message.handshake(0, 0).toArray

    println(s"${msg.length} and ${msg.toList}")

    socket.getOutputStream.write(msg)
    socket.getOutputStream.flush()
  }

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
