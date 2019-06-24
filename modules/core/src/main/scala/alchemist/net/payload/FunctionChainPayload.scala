package alchemist.net.payload

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

import alchemist.data.MatrixBlock
import alchemist.net.{ Datatype, MessagePayload }

private[net] final case class FunctionChainPayload(
  size: Int,
  builder: List[ByteBuffer => ByteBuffer]
) extends MessagePayload {

  override def putByte(value: Byte): MessagePayload = {
    val f: ByteBuffer => ByteBuffer = bb =>
      bb.put(Datatype.Byte.value)
        .put(value)

    FunctionChainPayload(size + 2, f :: builder)
  }

  override def putChar(value: Char): MessagePayload = {
    val f: ByteBuffer => ByteBuffer = bb =>
      bb.put(Datatype.Char.value)
        .put(value.toByte)

    FunctionChainPayload(size + 2, f :: builder)
  }

  override def putShort(value: Short): MessagePayload = {
    val f: ByteBuffer => ByteBuffer = bb =>
      bb.put(Datatype.Short.value)
        .putShort(value)

    FunctionChainPayload(size + 3, f :: builder)
  }

  override def putInt(value: Int): MessagePayload = {
    val f: ByteBuffer => ByteBuffer = bb =>
      bb.put(Datatype.Int.value)
        .putInt(value)

    FunctionChainPayload(size + 5, f :: builder)
  }

  override def putLong(value: Long): MessagePayload = {
    val f: ByteBuffer => ByteBuffer = bb =>
      bb.put(Datatype.Long.value)
        .putLong(value)

    FunctionChainPayload(size + 9, f :: builder)
  }

  override def putFloat(value: Float): MessagePayload = {
    val f: ByteBuffer => ByteBuffer = bb =>
      bb.put(Datatype.Float.value)
        .putFloat(value)

    FunctionChainPayload(size + 5, f :: builder)
  }

  override def putDouble(value: Double): MessagePayload = {
    val f: ByteBuffer => ByteBuffer = bb =>
      bb.put(Datatype.Double.value)
        .putDouble(value)

    FunctionChainPayload(size + 9, f :: builder)
  }

  override def putString(value: String): MessagePayload = {
    val f: ByteBuffer => ByteBuffer = bb => {
      bb.put(Datatype.String.value)
        .putShort(value.length.toShort)
        .put(value.getBytes(StandardCharsets.UTF_8))
    }

    FunctionChainPayload(size + 1 + 2 + value.length, f :: builder)
  }

  override def putIndexedRow(index: Long, length: Long, values: Array[Double]): MessagePayload = {
    val f: ByteBuffer => ByteBuffer = bb => {
      val zero = bb
        .put(Datatype.IndexedRow.value)
        .putLong(index)
        .putLong(length)

      values.foldLeft(zero)(_.putDouble(_))
    }

    val newSize = size + 1 + 16 + values.length * 8

    FunctionChainPayload(newSize, f :: builder)
  }

  override def putMatrixBlock(value: MatrixBlock): MessagePayload = {
    val f: ByteBuffer => ByteBuffer = bb => {

      val rows    = value.rows.foldLeft(bb.put(Datatype.MatrixBlock.value))(_.putLong(_))
      val columns = value.columns.foldLeft(rows)(_.putLong(_))

      value.data.foldLeft(columns)(_.putDouble(_))
    }

    val newSize = size + 1 + (value.rows.length + value.columns.length + value.data.length) * 8

    FunctionChainPayload(newSize, f :: builder)
  }

  override def toByteBuffer: ByteBuffer =
    Function.chain(builder.reverse).apply(MessagePayload.initBuffer(size))
}
