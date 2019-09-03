package alchemist.net.message.backend

import scodec.Decoder

import alchemist.data.Matrix.MatrixId
import alchemist.data.MatrixBlock
import alchemist.net.codecs.{ alchemistMatrixBlockCodec, alchemistMatrixIdCodec }
import alchemist.net.message.BackendMessage

final case class RequestMatrixBlock(matrixId: MatrixId, block: MatrixBlock) extends BackendMessage {
  println(this)
}

object RequestMatrixBlock {

  val decoder: Decoder[RequestMatrixBlock] =
    (alchemistMatrixIdCodec.withContext("matrix_id") :: alchemistMatrixBlockCodec.withContext("matrix_block")).as[RequestMatrixBlock].decodeOnly.withContext("request_matrix_block")
}
