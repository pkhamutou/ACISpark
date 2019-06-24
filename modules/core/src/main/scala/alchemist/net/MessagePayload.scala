package alchemist.net

import java.nio.{ ByteBuffer, ByteOrder }

import alchemist.data.MatrixBlock
import alchemist.net.payload.FunctionChainPayload

trait MessagePayload {

  def toByteBuffer: ByteBuffer

  def size: Int

  def putByte(value: Byte): MessagePayload

  def putChar(value: Char): MessagePayload

  def putShort(value: Short): MessagePayload

  def putInt(value: Int): MessagePayload

  def putLong(value: Long): MessagePayload

  def putFloat(value: Float): MessagePayload

  def putDouble(value: Double): MessagePayload

  def putString(value: String): MessagePayload

  def putMatrixBlock(value: MatrixBlock): MessagePayload

  def putIndexedRow(index: Long, length: Long, values: Array[Double]): MessagePayload
}

object MessagePayload {

  def initBuffer(size: Int): ByteBuffer =
    ByteBuffer.allocate(size).order(ByteOrder.BIG_ENDIAN)

  def empty: MessagePayload = FunctionChainPayload(0, Nil)
}
