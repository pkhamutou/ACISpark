package alchemist.net.payload

import org.scalatest.{ Assertion, FunSuite, Matchers }

import alchemist.net.{ Datatype, MessagePayload }

class FunctionChainPayloadTest extends FunSuite with Matchers {

  private val payload: FunctionChainPayload = FunctionChainPayload(0, Nil)

  private def check(result: MessagePayload, expectedSize: Int, expectedArray: Array[Byte]): Assertion = {
    val resultAsArray = result.toByteBuffer.array()
    val expected      = expectedArray

    resultAsArray should have size expectedSize
    (resultAsArray should contain).theSameElementsInOrderAs(expected)
  }

  test(".toByteBuffer should create a byte buffer with correct size") {
    val result       = payload.putByte(42).putShort(42).toByteBuffer.array()
    val expected     = Array[Byte](Datatype.Byte.value, 42, Datatype.Short.value, 0, 42)
    val expectedSize = 1 + 1 + 1 + 2

    result should have size expectedSize
    result should contain theSameElementsInOrderAs expected
  }

  test("should properly put byte") {
    val result       = payload.putByte(96)
    val expected     = Array[Byte](Datatype.Byte.value, 96)
    val expectedSize = 1 + 1

    check(result, expectedSize, expected)
  }

  test("should properly put char") {
    val result       = payload.putChar('x')
    val expected     = Array[Byte](Datatype.Char.value, 120)
    val expectedSize = 1 + 1

    check(result, expectedSize, expected)
  }

  test("should properly put int") {
    val result       = payload.putInt(42)
    val expected     = Array[Byte](Datatype.Int.value, 0, 0, 0, 42)
    val expectedSize = 1 + 4

    check(result, expectedSize, expected)
  }

  test("should properly put long") {
    val result       = payload.putLong(123)
    val expected     = Array[Byte](Datatype.Long.value, 0, 0, 0, 0, 0, 0, 0, 123)
    val expectedSize = 1 + 8

    check(result, expectedSize, expected)
  }

  test("should properly put float") {
    val result       = payload.putFloat(123.45f)
    val expected     = Array[Byte](Datatype.Float.value, 66, -10, -26, 102)
    val expectedSize = 1 + 4

    check(result, expectedSize, expected)
  }

  test("should properly put double") {
    val result       = payload.putDouble(123.45d)
    val expected     = Array[Byte](Datatype.Double.value, 64, 94, -36, -52, -52, -52, -52, -51)
    val expectedSize = 1 + 8

    check(result, expectedSize, expected)
  }

}
