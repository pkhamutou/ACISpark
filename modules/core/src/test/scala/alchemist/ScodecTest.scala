package alchemist

import org.scalatest.WordSpec
import scodec._
import scodec.bits.BitVector
import scodec.codecs._
import shapeless.{HList, HNil, ::}

import alchemist.net.message._
import alchemist.net.Command

class ScodecTest extends WordSpec {

  val bv = BitVector(Array[Byte]())

//  discriminated[Command].by(byte)

  val h = Header(ClientId(1), SessionId(1), Command.Handshake, 0, 0)

  val c = (Codec[Header] :: byte)

  val bb = Array[Byte](126, 126, 0, 1, 1, -5, 1, 0, 0)

  val f: Header :: Byte :: HNil = h :: 1.toByte :: HNil

}
