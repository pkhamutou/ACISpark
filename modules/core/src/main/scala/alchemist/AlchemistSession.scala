package alchemist

import java.net.Socket

import cats.effect.{ Resource, Sync }

import com.typesafe.scalalogging.LazyLogging
import scodec.bits.BitVector

import alchemist.net.{ Command, MessageHeader, MessagePayload }

class AlchemistSession(socket: Socket) extends LazyLogging {

  def handshake(): Unit = {
    import alchemist.net.message._
    val hsd = Header(ClientId(0), SessionId(0), Command.Handshake, 0, 0)
    val matrix = alchemist.data.MatrixBlock(
      data = (3 to 14).map(_ * 1.11d).toArray,
      rows = Array(0, 3, 1),
      columns = Array(0, 2, 1)
    )

    val hsb = Handshake(2, 1234, "ABCD", 1.11d, 2.22d, matrix, 190)

    val hsbv = Handshake.encoder.encode(hsb).require

    val bv = Header.codec.encode(hsd.copy(size = (hsbv.length / 8).toInt)).require ++ hsbv

    println(bv.toByteArray.toList)

//    val fms = alchemist.net.message.FrontendMessage.build(hsd)(alchemist.net.message.HandshakeResponse.codec)

    socket.getOutputStream.write(bv.toByteArray)
    socket.getOutputStream.flush()

    val header = Array.fill[Byte](10)(0)
    socket.getInputStream.read(header)

    val mh = Header.codec.decode(BitVector(header))

    println(mh)
    val payload = Array.fill[Byte](mh.require.value.size)(0)

    socket.getInputStream.read(payload)

    val pd = HandshakeOk.decoder.decode(BitVector(payload))

    println(pd)

    println("send")
  }

  def handshake2(): Unit = {
    val msg = alchemist.net.Message.handshake(0, 0).toArray

    println(msg.toList)
    socket.getOutputStream.write(msg)
    socket.getOutputStream.flush()

    val header = Array.fill[Byte](10)(0)
    val input  = socket.getInputStream

    input.read(header)
    println(header.toList)
//    val ss = alchemist.net.message.Header.header.decodeValue(BitVector.apply(header)).require
//    println(s"new header - $ss")
//    val mh = MessageHeader.decoder(header)
////    println(mh)
//
//    val payload = Array.fill[Byte](mh.size)(0)
//
//    input.read(payload)
//    println(payload.toList)
//    import java.nio.ByteBuffer
//    import alchemist.net.Datatype
//    val bb = ByteBuffer.wrap(payload)
//
//    println(Datatype.withValue(bb.get()))
//    println(bb.getShort())
//    println(Datatype.withValue(bb.get()))
//    println(bb.getShort())
//    val strs = Array.fill[Byte](4)(0)
//    bb.get(strs)
//    println(new String(strs))
//    println(Datatype.withValue(bb.get()))
//    println(bb.getDouble())
//    println(bb.position())

//    val m = alchemist.net.Message(MessageHeader(1, 1, Command.ListAllWorkers, 0, 0), MessagePayload.empty)
//
//    socket.getOutputStream.write(m.toArray)
//    socket.getOutputStream.flush()

  }

  def close(): Unit =
    logger.info("closing alchemist session!")
//    val m = alchemist.net.Message(MessageHeader(1, 1, Command.Shutdown, 0, 0), MessagePayload.empty)
//
//    socket.getOutputStream.write(m.toArray)
//    socket.getOutputStream.flush()

//    import scodec.bits.BitVector
//
//    val header = Array.fill[Byte](10)(0)
//    socket.getInputStream.read(header)
//
//    println(header.toList)
//
//    val ss = alchemist.net.message.Header.header.decodeValue(BitVector.apply(header)).require
//
//    println(ss)
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
