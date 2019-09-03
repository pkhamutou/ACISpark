package alchemist.net.message.backend

import scodec.Decoder

import alchemist.library.Param
import alchemist.net.message.BackendMessage

case class RunTask(args: List[Param]) extends BackendMessage

object RunTask {
  val decoder: Decoder[RunTask] = scodec.codecs.list(Param.codec).as[RunTask].asDecoder
}
