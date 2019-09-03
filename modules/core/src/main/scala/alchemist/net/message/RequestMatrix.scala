package alchemist.net.message

import alchemist.data.Matrix.MatrixId

final case class RequestMatrix(
  matrixId: MatrixId,
  start: Long,
  end: Long,
  rows2: Long,
  cols0: Long,
  cols1: Long,
  cols2: Long
)

object RequestMatrix {
  import scodec.Encoder
  import alchemist.net.codecs.{ alchemistLongCodec, alchemistMatrixIdCodec }

  implicit val encoder: Encoder[RequestMatrix] =
    (alchemistMatrixIdCodec :: alchemistLongCodec :: alchemistLongCodec :: alchemistLongCodec :: alchemistLongCodec :: alchemistLongCodec :: alchemistLongCodec)
      .as[RequestMatrix]
      .encodeOnly
}
