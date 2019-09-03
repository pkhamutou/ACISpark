package alchemist.net.message

import alchemist.data.Matrix.MatrixId
import alchemist.data.MatrixBlock

final case class SendMatrix(matrixId: MatrixId, block: Vector[MatrixBlock])

object SendMatrix {
  import scodec.Encoder
  import scodec.codecs._

  import alchemist.net.codecs.{ alchemistMatrixBlockCodec, alchemistMatrixIdCodec }

  implicit val encoder: Encoder[SendMatrix] =
    (alchemistMatrixIdCodec :: vector(alchemistMatrixBlockCodec)).as[SendMatrix].encodeOnly
}
