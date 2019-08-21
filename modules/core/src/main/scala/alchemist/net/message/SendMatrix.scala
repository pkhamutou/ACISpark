package alchemist.net.message

import alchemist.data.Matrix.MatrixId
import alchemist.data.MatrixBlock

final case class SendMatrix(
  matrixId: MatrixId,
  block: MatrixBlock,
  data: Vector[Double]
)

object SendMatrix {
  import scodec._
  import scodec.codecs._
  import alchemist.net.codecs._

  implicit val encoder: Encoder[SendMatrix] =
    (alchemistMatrixIdCodec :: alchemistMatrixBlockCodec :: vector(double)).as[SendMatrix].encodeOnly
}
