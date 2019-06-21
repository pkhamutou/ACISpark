package alchemist

import java.net.Socket

import cats.effect.{ Resource, Sync }

import com.typesafe.scalalogging.LazyLogging

import alchemist.net.{ MessageHeader, MessagePayload }

class AlchemistSession(socket: Socket) extends LazyLogging {

  def handshake(): Unit = {
    val msg = alchemist.net.Message.handshake(0, 0).toArray

    socket.getOutputStream.write(msg)
    socket.getOutputStream.flush()

    val header = Array.fill[Byte](10)(0)
    val input  = socket.getInputStream

    input.read(header)
    println(header.toList)
    val mh = MessageHeader.decoder(header)
    println(mh)

    val payload = Array.fill[Byte](mh.size)(0)

    input.read(payload)
    println(payload.toList)
    import java.nio.ByteBuffer
    import alchemist.net.Datatype
    val bb = ByteBuffer.wrap(payload)

    println(Datatype.withValue(bb.get()))
    println(bb.getShort())
    println(Datatype.withValue(bb.get()))
    println(bb.getShort())
    val strs = Array.fill[Byte](4)(0)
    bb.get(strs)
    println(new String(strs))
    println(Datatype.withValue(bb.get()))
    println(bb.getDouble())
    println(bb.position())

    val m = alchemist.net.Message(MessageHeader(1, 1, Command.ListAllWorkers, 0, 0), MessagePayload.empty)

    socket.getOutputStream.write(m.toArray)
    socket.getOutputStream.flush()

  }

  def close(): Unit = {
    logger.info("closing alchemist session!")
    val m = alchemist.net.Message(MessageHeader(1, 1, Command.CloseConnection, 0, 0), MessagePayload.empty)

    socket.getOutputStream.write(m.toArray)
    socket.getOutputStream.flush()
  }
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
