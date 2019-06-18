package alchemist.net

import alchemist.{ Command, MatrixBlock }

case class Message(header: MessageHeader, payload: MessagePayload) {

  def toArray: Array[Byte] =
    header.toByteBuffer.array() ++ payload.toByteBuffer.array()
}

object Message {

  def handshake(clientId: Short, sessionId: Short): Message = {

    val matrix: MatrixBlock = MatrixBlock(
      data = (3 to 14).map(_ * 1.11d).toArray,
      rows = Array(0, 3, 1),
      columns = Array(0, 2, 1)
    )

    val payload = MessagePayload.empty
      .putByte(2)
      .putShort(1234)
      .putString("ABCD")
      .putDouble(1.11d)
      .putDouble(2.22d)
      .putMatrixBlock(matrix)
      .putInt(MessageHeader.HeaderLength + 180)

    val header = MessageHeader(clientId, sessionId, Command.Handshake, 0, payload.size)

    Message(header, payload)
  }
}
