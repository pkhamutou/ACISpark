package alchemist.protocol

import java.nio.{ ByteBuffer, ByteOrder }

import cats.data.Kleisli
import cats.Eval

trait Message {

  protected def putDatatype(datatype: Datatype): Message = putByte(datatype.value)

  def toByteBuffer: ByteBuffer

  def putByte(value: Byte): Message

  def putChar(value: Char): Message

}

object Message {

  def initBuffer(size: Int): ByteBuffer = ByteBuffer.allocate(10 + size).order(ByteOrder.BIG_ENDIAN)

  def fcm: Message = FunctionChainMessage(0, Nil)
//  def km: Message  = KleisliMessage(0, Kleisli.ask[Eval, ByteBuffer])
}

case class FunctionChainMessage(size: Int, builder: List[ByteBuffer => ByteBuffer]) extends Message {

  private def chain(datatype: Datatype, size: Int, f: ByteBuffer => ByteBuffer): Message = {
    val addDatatype: ByteBuffer => ByteBuffer = _.put(datatype.value)
    FunctionChainMessage(size + 1, addDatatype.andThen(f) :: builder)
  }

  override def putByte(value: Byte): Message =
    FunctionChainMessage(size + 1, ((bb: ByteBuffer) => bb.put(value)) :: builder)

  override def putChar(value: Char): Message =
    chain(Datatype.Char, size + 1, _.put(value.toByte))


  override def toByteBuffer: ByteBuffer =
    Function.chain(builder.reverse).apply(Message.initBuffer(size))
}

/*case class KleisliMessage(size: Int, builder: Kleisli[Eval, ByteBuffer, ByteBuffer]) extends Message {

  override def putByte(value: Byte): KleisliMessage = {
    val k = builder.andThen(bb => Eval.now(bb.put(value)))
    KleisliMessage(size + 1, k)
  }

  override def putByte(index: Int, value: Byte): Message = {
    val k = builder.andThen(bb => Eval.later(bb.put(index, value)))
    KleisliMessage(size + 1, k)
  }

  override def toByteBuffer: ByteBuffer = builder.run(Message.initBuffer(size)).value
}*/
