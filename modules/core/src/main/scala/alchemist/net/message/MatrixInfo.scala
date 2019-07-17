package alchemist.net.message

import scodec.Encoder
import scodec.codecs.StringEnrichedWithCodecContextSupport

import alchemist.net.codecs.{alchemistByteCodec, alchemistLayoutCodec, alchemistLongCodec, alchemistStringCodec}

final case class MatrixInfo(
  matrixName: String,
  numOfRows: Long,
  numOfColumns: Long,
  sparse: Byte,
  layout: Layout
)

object MatrixInfo {
  implicit val encoder: Encoder[MatrixInfo] = "matrix_info" | {
    ("matrix_name"    | alchemistStringCodec) ::
    ("num_of_rows"    | alchemistLongCodec) ::
    ("num_of_columns" | alchemistLongCodec) ::
    ("sparse"         | alchemistByteCodec) ::
    ("layout"         | alchemistLayoutCodec)
  }.as[MatrixInfo].encodeOnly
}
