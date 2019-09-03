package alchemist.net.message.backend

import scodec.Decoder

import alchemist.data.Matrix.MatrixId
import alchemist.net.codecs.{ alchemistIntCodec, alchemistMatrixIdCodec }
import alchemist.net.message.BackendMessage

// MatrixId/NumberOfRows(Long)
final case class SendIndexedRow(xs: List[Byte]) extends BackendMessage

object SendIndexedRow {
  val decoder: Decoder[SendIndexedRow] =
    scodec.codecs.listOfN(scodec.codecs.provide(8), scodec.codecs.byte).as[SendIndexedRow].asDecoder
}
