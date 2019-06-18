package alchemist.net

import java.nio.{ ByteBuffer, ByteOrder }

import alchemist.Command

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
}
