package alchemist.net.codecs

import java.nio.ByteBuffer

import org.scalatest.{Matchers, WordSpec}
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import scodec.{Attempt, DecodeResult, Encoder, SizeBound}
import scodec.bits.BitVector

import alchemist.net.message.{Datatype, Layout}
import alchemist.AlchemistSession
import alchemist.net.message.backend.GetMatrixHandle

class CodecsTest extends WordSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 1000)

  implicit class AttemptOps(a: Attempt[BitVector]) {
    def array(): Array[Byte] = a.require.toByteArray
    def list(): List[Byte]   = a.require.toByteArray.toList
  }

  "alchemistByteCodec" should {
    "test" in {

      val data = List[Byte](54, 0, 1, 0, 3, 78, 101, 111, 0, 0, 0, 0, 0, 0, 0, 20, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 1,
        0, 2, 0, 1, 0, 0, 0, 0, 0, 2, 0, 0, 0, 1)

      val input = BitVector(data.toArray)

      println(GetMatrixHandle.decoder.decode(input))

      println(Layout.MC_MR.entryName)
    }

    "properly encode and decode data" in forAll("byte") { b: Byte =>
      val bb = ByteBuffer.allocate(2).put(Datatype.Byte.value).put(b).array()
      val b2 = ByteBuffer.allocate(2).put(Datatype.Int.value).put(b).array()

      alchemistByteCodec.encode(b).array() should contain theSameElementsInOrderAs bb

//      println(alchemistByteCodec.decode(BitVector(b2)))
    }
  }
}
