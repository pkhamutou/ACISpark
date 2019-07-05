package alchemist.net.codecs

import java.nio.ByteBuffer

import org.scalatest.{ Matchers, WordSpec }
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scodec.{ Attempt, DecodeResult, Encoder, SizeBound }
import scodec.bits.BitVector

import alchemist.net.message.Datatype
import alchemist.AlchemistSession

class CodecsTest extends WordSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 1000)

  implicit class AttemptOps(a: Attempt[BitVector]) {
    def array(): Array[Byte] = a.require.toByteArray
    def list(): List[Byte]   = a.require.toByteArray.toList
  }

  "alchemistByteCodec" should {
    "test" in {

      import scodec.{ Codec, Decoder }
      import scodec.codecs._

    }

    "properly encode and decode data" in forAll("byte") { b: Byte =>
      val bb = ByteBuffer.allocate(2).put(Datatype.Byte.value).put(b).array()
      val b2 = ByteBuffer.allocate(2).put(Datatype.Int.value).put(b).array()

      alchemistByteCodec.encode(b).array() should contain theSameElementsInOrderAs bb

//      println(alchemistByteCodec.decode(BitVector(b2)))
    }
  }
}
