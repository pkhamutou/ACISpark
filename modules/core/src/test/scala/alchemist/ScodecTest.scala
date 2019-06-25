package alchemist

import org.scalatest.WordSpec
import scodec._
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{::, HList, HNil}

import alchemist.net.message.{Command, _}
class ScodecTest extends WordSpec {


  val h = Header(ClientId(1), SessionId(1), Command.Handshake, 0, 0)

  val c = (Codec[Header] :: byte)

  val bb = Array[Byte](126, 126, 0, 1, 1, -5, 1, 0, 0)

  val bv = BitVector(bb)

  println(bv.toByteArray.toList)
  println(bv.dropRight(4 * 8).toByteArray.toList)

}
