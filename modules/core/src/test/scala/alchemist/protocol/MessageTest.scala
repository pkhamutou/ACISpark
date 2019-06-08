package alchemist.protocol
import org.scalatest.FunSuite

class MessageTest extends FunSuite {

  private val max   = 10000000
  private val range = 0 to max

  test("Test that FunctionChainMessage works") {

    val m = range.foldLeft(Message.fcm)((message, v) => message.putByte(v.toByte))

    m.toByteBuffer.array().length == max
  }

  test("Test that KleisliMessage works") {

    val m = range.foldLeft(Message.km)((message, v) => message.putByte(v.toByte))

    m.toByteBuffer.array().length == max
  }
}
