package alchemist.net

import java.nio.{ ByteBuffer, ByteOrder }

case class MessageHeader(clientId: Short, sessionId: Short, command: Command, error: Byte, size: Int) {

  def toByteBuffer: ByteBuffer =
    ByteBuffer
      .allocate(10)
      .order(ByteOrder.BIG_ENDIAN)
      .putShort(clientId)
      .putShort(sessionId)
      .put(command.value)
      .put(error)
      .putInt(size)
}

object MessageHeader {
  final def HeaderLength: Int = 10

  def decoder(a: Array[Byte]): MessageHeader = {
    val bb = ByteBuffer.wrap(a)

    MessageHeader(
      clientId = bb.getShort(),
      sessionId = bb.getShort(),
      command = Command.withValue(bb.get()),
      error = bb.get(),
      size = bb.getInt()
    )
  }
}
