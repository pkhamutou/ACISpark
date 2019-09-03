package alchemist.net.message.backend

import scodec.Decoder

import alchemist.data.Matrix.MatrixId
import alchemist.data.MatrixBlock
import alchemist.net.codecs.{ alchemistMatrixBlockCodec, alchemistMatrixIdCodec }
import alchemist.net.message.BackendMessage

final case class RequestMatrixBlock(matrixId: MatrixId, blocks: Vector[MatrixBlock]) extends BackendMessage

object RequestMatrixBlock {

  val decoder: Decoder[RequestMatrixBlock] =
    (alchemistMatrixIdCodec.withContext("matrix_id") ::
      scodec.codecs.vector(alchemistMatrixBlockCodec).withContext("matrix_blocks"))
      .as[RequestMatrixBlock]
      .decodeOnly
      .withContext("request_matrix_block")
}
