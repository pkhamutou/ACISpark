package alchemist.library

import scodec.{Attempt, Codec, Decoder, DecodeResult, GenCodec, SizeBound}
import scodec.bits.BitVector

import alchemist.net.codecs._
import alchemist.net.message.Datatype

sealed trait Param

object Param {
  // TODO: Learn how to use shapeless!!!

  private def getParamCodec[A](codec: Codec[A]) = getCodec(Datatype.Parameter, codec)

  final case class PByte(name: String, value: Byte) extends Param

  object PByte {
    val codec: Codec[PByte] = getParamCodec(alchemistStringCodec :: alchemistByteCodec).as[PByte]
  }

  final case class PChar(name: String, value: Char) extends Param

  object PChar {
    val codec: Codec[PChar] = getParamCodec(alchemistStringCodec :: alchemistCharCodec).as[PChar]
  }

  final case class PShort(name: String, value: Short) extends Param

  object PShort {
    val codec: Codec[PShort] = getParamCodec(alchemistStringCodec :: alchemistShortCodec).as[PShort]
  }

  final case class PInt(name: String, value: Int) extends Param

  object PInt {
    val codec: Codec[PInt] = getParamCodec(alchemistStringCodec :: alchemistIntCodec).as[PInt]
  }

  final case class PLong(name: String, value: Long) extends Param

  object PLong {
    val codec: Codec[PLong] = getParamCodec(alchemistStringCodec :: alchemistLongCodec).as[PLong]
  }

  final case class PFloat(name: String, value: Float) extends Param

  object PFloat {
    val codec: Codec[PFloat] = getParamCodec(alchemistStringCodec :: alchemistFloatCodec).as[PFloat]
  }

  final case class PDouble(name: String, value: Double) extends Param

  object PDouble {
    val codec: Codec[PDouble] = getParamCodec(alchemistStringCodec :: alchemistDoubleCodec).as[PDouble]
  }

  final case class PString(name: String, value: String) extends Param

  object PString {
    val codec: Codec[PString] = getParamCodec(alchemistStringCodec :: alchemistStringCodec).as[PString]
  }

  final def apply[A](name: String, value: A): Param = value match {
    case v: Byte   => PByte(name, v)
    case v: Char   => PChar(name, v)
    case v: Short  => PShort(name, v)
    case v: Int    => PInt(name, v)
    case v: Long   => PLong(name, v)
    case v: Float  => PFloat(name, v)
    case v: Double => PDouble(name, v)
    case v: String => PString(name, v)
    case _         => throw new IllegalArgumentException(s"Param of type ${value.getClass.getTypeName} is not supported")
  }

  val codec: Codec[Param] = new Codec[Param] {

    private val allCodecs: List[GenCodec[_, Param]] = List(
      PByte.codec,
      PChar.codec,
      PShort.codec,
      PInt.codec,
      PLong.codec,
      PFloat.codec,
      PDouble.codec,
      PString.codec
    )

    override val sizeBound: SizeBound =
      SizeBound.choice(allCodecs.map(_.sizeBound))

    override def decode(bits: BitVector): Attempt[DecodeResult[Param]] =
      Decoder.choiceDecoder(allCodecs.map(_.asDecoder): _*).decode(bits)

    override def encode(value: Param): Attempt[BitVector] = value match {
      case v: PByte   => PByte.codec.encode(v)
      case v: PChar   => PChar.codec.encode(v)
      case v: PShort  => PShort.codec.encode(v)
      case v: PInt    => PInt.codec.encode(v)
      case v: PLong   => PLong.codec.encode(v)
      case v: PFloat  => PFloat.codec.encode(v)
      case v: PDouble => PDouble.codec.encode(v)
      case v: PString => PString.codec.encode(v)
    }
  }
}
