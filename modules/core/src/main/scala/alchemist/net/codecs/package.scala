package alchemist.net

import scodec._
import scodec.bits.BitVector
import scodec.codecs._

import alchemist.data.MatrixBlock

package object codecs {

  private val long64: Codec[Long] = long(64)

  def getCodec[A](dt: Datatype, codec: Codec[A]): Codec[A] = {

    val dropDatatype: (Datatype, A) => A = (_, a) => a

    val addDatatype: A => (Datatype, A) = (dt, _)

    alchemistDatatypeCodec
      .flatZip[A] {
        case `dt`        => codec
        case notExpected => fail[A](Err(s"Expected ${dt.entryName} but got ${notExpected.entryName}"))
      }
      .xmap(dropDatatype, addDatatype)
  }

  val alchemistDatatypeCodec: Codec[Datatype] = byte.xmap(Datatype.withValue, _.value)

  val alchemistByteCodec: Codec[Byte]   = getCodec(Datatype.Byte, byte)
  val alchemistShortCodec: Codec[Short] = getCodec(Datatype.Short, short16)
  val alchemistIntCodec: Codec[Int]     = getCodec(Datatype.Int, int32)
  val alchemistLongCodec: Codec[Long]   = getCodec(Datatype.Long, long(64))

  val alchemistFloatCodec: Codec[Float]   = getCodec(Datatype.Float, float)
  val alchemistDoubleCodec: Codec[Double] = getCodec(Datatype.Double, double)

  val alchemistCharCodec: Codec[Char] = getCodec(Datatype.Char, byte.xmap[Char](_.toChar, _.toByte))

  val alchemistStringCodec: Codec[String] = {

    val dropLength: (Short, String) => String = (_, str) => str

    val addLength: String => (Short, String) = str => (str.length.toShort, str)

    val codec = short16
      .flatZip(fixedSizeBytes[String](_, utf8))
      .xmap[String](dropLength, addLength)

    getCodec(Datatype.String, codec)
  }

  val alchemistMatrixBlockCodec: Codec[MatrixBlock] = {

    val encoder: Encoder[MatrixBlock] = new Encoder[MatrixBlock] {

      override def encode(value: MatrixBlock): Attempt[BitVector] =
        for {
          rows <- Encoder.encodeSeq(long64)(value.rows.toIndexedSeq)
          cols <- Encoder.encodeSeq(long64)(value.columns.toIndexedSeq)
          data <- Encoder.encodeSeq(double)(value.data.toIndexedSeq)
        } yield rows ++ cols ++ data
      override def sizeBound: SizeBound = SizeBound.unknown
    }

    val decoder: Decoder[MatrixBlock] = new Decoder[MatrixBlock] {

      private def calculate(a: IndexedSeq[Long]): Long =
        math.ceil(1.0 * ((a(1) - a(0)) / a(2)) + 1).toLong

      override def decode(bits: BitVector): Attempt[DecodeResult[MatrixBlock]] =
        for {
          rowsDR <- Decoder.decodeCollect(long64, Some(3))(bits)
          rows = rowsDR.value
          colsDR <- Decoder.decodeCollect(long64, Some(3))(rowsDR.remainder)
          cols = colsDR.value
          size = calculate(rows) * calculate(cols)
          data <- Decoder.decodeCollect(double, Some(size.toInt))(colsDR.remainder)
        } yield data.map(d => MatrixBlock(d.toArray, rows.toArray, cols.toArray))
    }

    getCodec(Datatype.MatrixBlock, Codec(encoder, decoder))
  }

}
