package alchemist.net.message.backend

import alchemist.data.Matrix
import alchemist.net.codecs.alchemistMatrixCodec
import alchemist.net.message.BackendMessage

case class GetMatrixHandle(matrix: Matrix) extends BackendMessage

object GetMatrixHandle {
  val decoder = alchemistMatrixCodec.as[GetMatrixHandle]
}
